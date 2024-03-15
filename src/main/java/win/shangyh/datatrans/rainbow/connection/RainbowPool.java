package win.shangyh.datatrans.rainbow.connection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

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

public class RainbowPool {

    private final static Logger logger = LoggerFactory.getLogger(RainbowPool.class);

    /**
     * 默认连接可用性检测的超时时间 / 秒
     */
    private final static int DEFAULT_TIME_OUT = 120;

    // private final Map<ManagedConnection, TimedDbInfo> connCache = new ConcurrentHashMap<>();

    /**
     * 1分钟的时长
     */
    private final static int MINUTES = 60 * 1000;

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
    
    public RainbowPool(DatabaseInfo databaseInfo, int maxConnCount) {
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
        GenericKeyedObjectPoolConfig<ManagedConnection> config = new GenericKeyedObjectPoolConfig<>();
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
            ManagedConnection connection = pool.borrowObject(this.databaseInfo);
            return connection;
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
    public void returnConnection(ManagedConnection connection) {
        pool.returnObject(databaseInfo, connection);
    }

    class ObservableObjectPool extends GenericKeyedObjectPool<DatabaseInfo, ManagedConnection> {
        public ObservableObjectPool(KeyedPooledObjectFactory<DatabaseInfo, ManagedConnection> factory,
                final GenericKeyedObjectPoolConfig<ManagedConnection> config) {
            super(factory, config);
        }

        @Override
        public ManagedConnection borrowObject(DatabaseInfo key) throws Exception {
            ManagedConnection connection = super.borrowObject(key);
            return connection;
        }

        @Override
        public ManagedConnection borrowObject(DatabaseInfo key, long borrowMaxWaitMillis) throws Exception {
            ManagedConnection connection = super.borrowObject(key, borrowMaxWaitMillis);
            return connection;
        }
    }

    class ConnectionFactory extends BaseKeyedPooledObjectFactory<DatabaseInfo, ManagedConnection> {
        @Override
        public ManagedConnection create(DatabaseInfo key) {
            Connection connection = DBUtils.newDbConnection(key.getJdbcUrl(), key.getAccount(), key.getPassword());
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                logger.error("数据库连接设置自动提交失败", e);
            }
            return new ManagedConnection(connection, RainbowPool.this, key);
        }

        @Override
        public PooledObject<ManagedConnection> wrap(ManagedConnection value) {
            return new DefaultPooledObject<>(value);
        }

        @Override
        public void destroyObject(DatabaseInfo key, PooledObject<ManagedConnection> p) {
            Connection conn = p.getObject();
            if (conn == null) {
                return;
            }
            ManagedConnection managedConnection = (ManagedConnection) conn;
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
        public boolean validateObject(DatabaseInfo key, PooledObject<ManagedConnection> p) {
            ManagedConnection conn = p.getObject();
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
        public void passivateObject(DatabaseInfo key, PooledObject<ManagedConnection> p) throws Exception {
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
        pool.close();
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
