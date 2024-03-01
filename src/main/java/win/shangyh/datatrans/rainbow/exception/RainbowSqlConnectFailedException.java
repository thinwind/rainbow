package win.shangyh.datatrans.rainbow.exception;

/**
 * 在SQL数据源连接失败时会抛出此异常
 *
 * 如果需要针对无法连接的数据源进行处理
 * 需要捕获此异常
 *
 * 不使用SQLException的原因：
 * SQLException在jdbc执行过程中，会有
 * 多个地方抛出，比如连接失败，事务提交失败
 * SQL执行失败，连接关闭失败等。SQLException
 * 无法唯一的定位到连接的异常问题。
 * 此异常仅在无法建立连接时抛出来，可以用作
 * 连接建立失败的标记
 */
public class RainbowSqlConnectFailedException extends RuntimeException{

    private static final long serialVersionUID = 1595734609151830344L;

    public RainbowSqlConnectFailedException() {
        super();
    }

    public RainbowSqlConnectFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public RainbowSqlConnectFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public RainbowSqlConnectFailedException(Throwable cause) {
        super(cause);
    }
}
