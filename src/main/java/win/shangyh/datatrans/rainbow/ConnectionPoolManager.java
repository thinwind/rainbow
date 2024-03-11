package win.shangyh.datatrans.rainbow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObjectInfo;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import win.shangyh.datatrans.rainbow.exception.RainbowSqlConnectFailedException;
import win.shangyh.datatrans.rainbow.util.DBUtils;
import win.shangyh.datatrans.rainbow.util.DateUtil;

public class ConnectionPoolManager {

    private final static Logger logger = LoggerFactory.getLogger(ConnectionPoolManager.class);

    /**
     * 默认连接可用性检测的超时时间 / 秒
     */
    private final static int DEFAULT_TIME_OUT = 120;

    private final Map<Connection, TimedDbInfo> connCache = new ConcurrentHashMap<>();

    /**
     * 1分钟的时长
     */
    private final static int MINUTES = 60 * 1000;

    /**
     * 检测连接超时机制
     * <p>
     * 如果一个连接从开始使用，到检测时超过了这个时间
     * 就把它kill掉，不论是否有任务在执行
     */
    private static final long CONNECTION_TIMEOUT =2 * 60 * MINUTES;

    /**
     * 连接池默认的检测间隔时间
     */
    private final static long DEFAULT_CHECK_TIME = 5 * MINUTES;

    /**
     * 默认的连接空闲时长
     *
     * 超过此时间没有被使用，连接将被释放
     */
    private final static long DEFAULT_IDLE_TIMEOUT = 20 * MINUTES;

    static final ThreadPoolExecutor CONNECTION_MANIPULATE_EXECUTOR;

    final ObservableObjectPool pool;

    final DatabaseInfo databaseInfo;
    
    final int maxConnCount;
    
    java.util.concurrent.CopyOnWriteArraySet

    public ConnectionPoolManager(DatabaseInfo databaseInfo, int maxConnCount) {
        this.databaseInfo = databaseInfo;
        this.maxConnCount = maxConnCount;
        pool = buildConnectionPool();
    }

    static {
        ThreadFactory namedThreadFactory = new DbConnectionThreadFactory();
        CONNECTION_MANIPULATE_EXECUTOR = new ThreadPoolExecutor(1, 4, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(512), namedThreadFactory, new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    ObservableObjectPool buildConnectionPool() {
        //初始化连接池
        GenericKeyedObjectPoolConfig<Connection> config = new GenericKeyedObjectPoolConfig<>();
        config.setFairness(false);
        //连接超时时间
        config.setMinEvictableIdleDuration(Duration.ofMillis(DEFAULT_IDLE_TIMEOUT));
        //检测间隔时间
        config.setTimeBetweenEvictionRuns(Duration.ofMillis(DEFAULT_CHECK_TIME));
        config.setMaxTotal(maxConnCount);

        logger.info("连接池初始化设置:");
        logger.info("Fairness: {}", config.getFairness());
        logger.info("MinEvictableIdleDuration: {}", config.getMinEvictableIdleDuration().toSeconds());
        logger.info("TimeBetweenEvictionRuns: {}", config.getDurationBetweenEvictionRuns().toSeconds());
        logger.info("MaxTotal: {}", config.getMaxTotal());

        return new ObservableObjectPool(new ConnectionFactory(), config);
    }

    public Map<String, List<DefaultPooledObjectInfo>> inspectConnectionPool() {
        return pool.listAllObjects();
    }

    public Connection getPooledConnection() {
        try {
            return pool.borrowObject(this.databaseInfo);
        } catch (Exception e) {
            throw new RainbowSqlConnectFailedException(e);
        }
    }

    /**
     * 在强制关闭连接后，可能会导致异常
     *
     * @param databaseInfo
     * @param connection
     */
    public void returnConnection(Connection connection) {
        boolean returned = false;
        try {
            if (connection != null && !connection.isClosed() && connection.isValid(DEFAULT_TIME_OUT)) {
                // 在正常归还连接之后
                // 一定要及时从检测中清理掉
                // 否则会被强制关闭
                connection.commit();
                pool.returnObject(databaseInfo, connection);
                connCache.remove(connection);
                returned = true;
            }
        } catch (Exception e) {
            logger.warn("归还连接失败,连接信息:{}", databaseInfo.getJdbcUrl(), e);
        }
        if (!returned) {
            try {
                //从连接池中移除
                pool.invalidateObject(databaseInfo, connection);
                connCache.remove(connection);
            } catch (Exception e) {
                logger.error("从连接池移除连接失败,连接信息:{}", databaseInfo.getJdbcUrl(), e);
            }
        }
    }

    /**
     * 净化连接
     * <p>
     * 将执行时长超过预设时间的连接强制放弃
     */
    public void pureConnections() {
        //仅放弃超时的连接
        abortConnections(false);
    }

    public void clearCache(boolean closeAllConnections) {
        //仅清空缓存的对象
        if (!closeAllConnections) {
            connCache.clear();
            return;
        }

        //中止所有的连接
        abortConnections(true);
        //清理连接池
        clearConnPool();
    }

    private void abortConnections(boolean killAll) {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Connection, TimedDbInfo> entry : connCache.entrySet()) {
            TimedDbInfo info = entry.getValue();
            if (info == null) {
                continue;
            }

            //执行超过允许时长，直接关闭连接
            //关闭连接之后，由returnObject方法负责清理监控即可
            boolean kill = killAll || (Duration.between(info.time, now).toMillis() > CONNECTION_TIMEOUT);
            if (kill) {
                Connection conn = entry.getKey();
                try {
                    logger.warn("强制终止数据库连接:{}", info.key.toString());
                    conn.abort(CONNECTION_MANIPULATE_EXECUTOR);
                } catch (SQLException | RuntimeException e) {
                    //仅记录异常信息，不要影响后续的执行过程
                    logger.error("停止连接异常", e);
                } finally {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        //如果conn.abort调用成功，此时检测，会导致异常
                        //认为已关闭即可
                    }
                }
            }
        }
    }

    public Collection<String> inspectConnectionCache() {
        return connCache.values().stream().map(item -> item.toString()).collect(Collectors.toList());
    }

    class ObservableObjectPool extends GenericKeyedObjectPool<DatabaseInfo, Connection> {
        public ObservableObjectPool(KeyedPooledObjectFactory<DatabaseInfo, Connection> factory,
                final GenericKeyedObjectPoolConfig<Connection> config) {
            super(factory, config);
        }

        @Override
        public Connection borrowObject(DatabaseInfo key) throws Exception {
            Connection connection = super.borrowObject(key);
            connCache.put(connection, new TimedDbInfo(key, LocalDateTime.now()));
            return connection;
        }

        @Override
        public Connection borrowObject(DatabaseInfo key, long borrowMaxWaitMillis) throws Exception {
            Connection connection = super.borrowObject(key, borrowMaxWaitMillis);
            connCache.put(connection, new TimedDbInfo(key, LocalDateTime.now()));
            return connection;
        }
    }

    class ConnectionFactory extends BaseKeyedPooledObjectFactory<DatabaseInfo, Connection> {
        @Override
        public Connection create(DatabaseInfo key) {
            Connection connection = DBUtils.newDbConnection(key.getJdbcUrl(), key.getAccount(), key.getPassword());
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                logger.error("数据库连接设置自动提交失败", e);
            }
            return new ManagedConnection(connection, ConnectionPoolManager.this);
        }

        @Override
        public PooledObject<Connection> wrap(Connection value) {
            return new DefaultPooledObject<>(value);
        }

        @Override
        public void destroyObject(DatabaseInfo key, PooledObject<Connection> p) {
            Connection conn = p.getObject();
            if (conn == null) {
                return;
            }
            ManagedConnection managedConnection = (ManagedConnection) conn;
            connCache.remove(managedConnection);
            try {
                if (!conn.isClosed() && !conn.getAutoCommit()) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        logger.error("数据库连接提交失败", e);
                    }

                    try {
                        managedConnection.realClose();
                    } catch (SQLException e) {
                        logger.error("数据库连接关闭失败", e);
                    }
                }
            } catch (SQLException e) {
                logger.error("数据库连接池对象销毁前检测异常", e);
            }
        }

        @Override
        public boolean validateObject(DatabaseInfo key, PooledObject<Connection> p) {
            Connection conn = p.getObject();
            try {
                return conn != null && !conn.isClosed() && conn.isValid(DEFAULT_TIME_OUT);
            } catch (SQLException e) {
                logger.error("连接可用性检测失败", e);
                return false;
            }
        }

        @Override
        /**
         * 钝化操作
         * 归还Connection时调用
         * 钝化前，提交之前的sql事务
         */
        public void passivateObject(DatabaseInfo key, PooledObject<Connection> p) throws Exception {
            Connection conn = p.getObject();
            if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
                try {
                    conn.commit();
                } catch (SQLException e) {
                    logger.error("数据库连接提交失败", e);
                }
            }
        }
    }

    public void clearConnPool() {
        pool.clear();
    }

    public void closeConnPool() {
        abortConnections(true);
        pool.close();
    }

    static class TimedDbInfo {

        public TimedDbInfo(DatabaseInfo key, LocalDateTime time) {
            this.key = key;
            this.time = time;
        }

        final DateUtil dateUtil = new DateUtil(null, null, null);

        final DatabaseInfo key;

        final LocalDateTime time;

        @Override
        public String toString() {
            return new StringJoiner(", ", "连接信息[", "]")
                    .add("DbInfo" + key)
                    .add("LastInvocation" + dateUtil.formatDateTime(time))
                    .toString();
        }
    }

    private static class DbConnectionThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DbConnectionThreadFactory() {
            namePrefix = "db-conn-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(null, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
