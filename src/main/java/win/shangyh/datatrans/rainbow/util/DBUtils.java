package win.shangyh.datatrans.rainbow.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import win.shangyh.datatrans.rainbow.exception.RainbowSqlConnectFailedException;

public class DBUtils {
    
    private final static Logger logger = LoggerFactory.getLogger(DBUtils.class);
    
    //默认连接超时时长15秒
    public static final int DEFAULT_TIME_OUT = 15;//unit:senconds


    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Class.forName("win.shangyh.datatrans.rainbow.transfer.ByteTransfer");
            Class.forName("win.shangyh.datatrans.rainbow.transfer.DecimalTransfer");
            Class.forName("win.shangyh.datatrans.rainbow.transfer.IntegerTransfer");
            Class.forName("win.shangyh.datatrans.rainbow.transfer.LongTransfer");
            Class.forName("win.shangyh.datatrans.rainbow.transfer.VarcharTransfer");
            Class.forName("win.shangyh.datatrans.rainbow.transfer.XlobTransfer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        DriverManager.setLoginTimeout(DEFAULT_TIME_OUT);
    }


    public static Connection newDbConnection(String url, String user, String password) {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            logger.error("数据库连接失败,url:{},user:{}", url, user, password,e);
            throw new RainbowSqlConnectFailedException(e);
        }
    }

}
