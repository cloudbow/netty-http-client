


package pns.alltypes.netty.httpclient.factory;

import java.util.concurrent.ThreadFactory;

/**
 * A factory for creating NHTTPClientThread objects.
 */
public class NHTTPClientThreadFactory implements ThreadFactory {
    
    /** The pool name. */
    private final String poolName;

    /** Instantiates a new NHTTP client thread factory.
     * @param poolName
     *            the pool name
     */
    public NHTTPClientThreadFactory(final String poolName) {
        this.poolName = poolName;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(final Runnable runnable) {
        return new NHTTPClientThread(runnable, poolName);
    }
}