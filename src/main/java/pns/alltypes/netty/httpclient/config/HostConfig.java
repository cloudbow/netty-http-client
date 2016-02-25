/**
 *
 */
package pns.alltypes.netty.httpclient.config;

/**
 * The Class HostConfig.
 * @author arung
 */
public class HostConfig {

    /** The host. */
    private String host;

    /** The port. */
    private int port = 80;

    /** The secure. */
    private boolean secure;

    /** The total connections. */
    private int totalConnections = 2;

    /** The total consumers. */
    private int totalConsumers = 3;

    /** The connect time out. */
    private int connectTimeOut = 1000;

    /** The read timeout. */
    private int readTimeout = 6000;

    /** The retry time. */
    private int retryTime = 2000;

    /** The request attempt timeout. */
    private final long requestAttemptTimeout = 24000;

    /** The sync type. */
    private SyncType syncType = SyncType.PERSISTENT;

    /** Instantiates a new host config.
    * @param host
    *            the host
    */
    public HostConfig(final String host) {
        this(host, 80);
    }

    /** Instantiates a new host config.
    * @param host
    *            the host
    * @param port
    *            the port
    */
    public HostConfig(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    /** Instantiates a new host config.
    * @param host
    *            the host
    * @param port
    *            the port
    * @param syncType2
    *            the sync type2
    */
    public HostConfig(final String host, final int port, final SyncType syncType2) {
        this(host, port);
        this.syncType = syncType2;
    }
    
    public String getBaseHttpUrl() {
       return new StringBuilder().append("http://").append(host).append(":").append(port).toString();
    }

    /** Gets the host.
    * @return the host
    */
    public String getHost() {
        return this.host;
    }

    /** Sets the host.
    * @param host
    *            the host to set
    */
    public void setHost(final String host) {
        this.host = host;
    }

    /** Gets the port.
    * @return the port
    */
    public int getPort() {
        return this.port;
    }

    /** Sets the port.
    * @param port
    *            the port to set
    */
    public void setPort(final int port) {
        this.port = port;
    }

    /** Checks if is secure.
    * @return the secure
    */
    public boolean isSecure() {
        return this.secure;
    }

    /** Sets the secure.
    * @param secure
    *            the secure to set
    */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(getHost());

    }

    /** Gets the total connections.
    * @return the total connections
    */
    public int getTotalConnections() {
        return totalConnections;
    }

    /** Sets the total connections.
    * @param totalConnections
    *            the new total connections
    */
    public void setTotalConnections(final int totalConnections) {
        this.totalConnections = totalConnections;
    }

    /** Gets the connect time out.
    * @return the connect time out
    */
    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    /** Sets the connect time out.
    * @param connectTimeOut
    *            the new connect time out
    */
    public void setConnectTimeOut(final int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    /** Gets the read timeout.
    * @return the read timeout
    */
    public int getReadTimeout() {
        return readTimeout;
    }

    /** Sets the read timeout.
    * @param readTimeout
    *            the new read timeout
    */
    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /** Gets the retry time.
    * @return the retry time
    */
    public int getRetryTime() {
        return retryTime;
    }

    /** Sets the retry time.
    * @param retryTime
    *            the new retry time
    */
    public void setRetryTime(final int retryTime) {
        this.retryTime = retryTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(String.format("[Host: %s, Port: %s]", host, port));
    }

    /** Gets the request attempt timeout.
    * @return the request attempt timeout
    */
    public long getRequestAttemptTimeout() {
        // TODO Auto-generated method stub
        return requestAttemptTimeout;
    }

    /** Gets the sync type.
    * @return the sync type
    */
    public SyncType getSyncType() {
        return syncType;
    }

    /** Sets the sync type.
    * @param syncType
    *            the new sync type
    */
    public void setSyncType(final SyncType syncType) {
        this.syncType = syncType;
    }

    /** Gets the total consumers.
    * @return the total consumers
    */
    public int getTotalConsumers() {
        return totalConsumers;
    }

    /** Sets the total consumers.
    * @param totalConsumers
    *            the new total consumers
    */
    public void setTotalConsumers(final int totalConsumers) {
        this.totalConsumers = totalConsumers;
    }

}
