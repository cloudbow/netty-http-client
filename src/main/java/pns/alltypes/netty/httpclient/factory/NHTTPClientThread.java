/**
 *
 */
package pns.alltypes.netty.httpclient.factory;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * The Class NHTTPClientThread.
 * @author arung
 */
public class NHTTPClientThread extends Thread {

    /** The Constant DEFAULT_NAME. */
    public static final String DEFAULT_NAME = "PnsAppThread";
    
    /** The Constant created. */
    private static final AtomicInteger created = new AtomicInteger();
    
    /** The Constant alive. */
    private static final AtomicInteger alive = new AtomicInteger();
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(NHTTPClientThread.class);

    /** Instantiates a new NHTTP client thread.
     * @param r
     *            the r
     */
    public NHTTPClientThread(final Runnable r) {
        this(r, NHTTPClientThread.DEFAULT_NAME);
    }

    /** Instantiates a new NHTTP client thread.
     * @param runnable
     *            the runnable
     * @param name
     *            the name
     */
    public NHTTPClientThread(final Runnable runnable, final String name) {
        super(runnable, name + "-" + NHTTPClientThread.created.incrementAndGet());
        setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                NHTTPClientThread.LOGGER.error("UNCAUGHT in thread " + t.getName(), e);
            }
        });
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        // Copy debug flag to ensure consistent value throughout.

        if (NHTTPClientThread.LOGGER.isTraceEnabled()) {
            NHTTPClientThread.LOGGER.debug("Created " + getName());
        }
        try {
            NHTTPClientThread.alive.incrementAndGet();
            super.run();
        } finally {
            NHTTPClientThread.alive.decrementAndGet();
            if (NHTTPClientThread.LOGGER.isTraceEnabled()) {
                NHTTPClientThread.LOGGER.trace("Exiting " + getName());
            }
        }
    }

    /** Gets the threads created.
     * @return the threads created
     */
    public static int getThreadsCreated() {
        return NHTTPClientThread.created.get();
    }

    /** Gets the threads alive.
     * @return the threads alive
     */
    public static int getThreadsAlive() {
        return NHTTPClientThread.alive.get();
    }

}
