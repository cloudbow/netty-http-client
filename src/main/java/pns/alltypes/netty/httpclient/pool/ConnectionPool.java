


package pns.alltypes.netty.httpclient.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.config.HostConfig;
import pns.alltypes.netty.httpclient.config.SyncType;
import pns.alltypes.netty.httpclient.exception.AlreadyRegisteredHostException;
import pns.alltypes.netty.httpclient.factory.NHTTPClientThreadFactory;
import pns.alltypes.netty.httpclient.request.HttpRequestIntializer;

/**
 * The Class BatchConnection.
 * @author arung
 */

public class ConnectionPool extends ConcurrentHashMap<String, BlockingQueue<Channel>> {
    
    /** The Constant TOT_MAX_CONN. */
    private static final AtomicInteger TOT_MAX_CONN = new AtomicInteger(0);

    /** The Constant CONNECTION_POOL. */
    // singleton
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool();

    /** Gets the single instance of ConnectionPool.
     * @return single instance of ConnectionPool
     */
    public static ConnectionPool getInstance() {
        return ConnectionPool.CONNECTION_POOL;
    }

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class);

    /** The Constant BOOTSTRAP_MAP. */
    private static final Map<HostConfig, Bootstrap> BOOTSTRAP_MAP = new ConcurrentHashMap<HostConfig, Bootstrap>();

    /** The atomic batch counter. */
    private static final AtomicInteger ATOM_BATCH_COUNTER = new AtomicInteger(0);

    /** The batch nio event loop group. */
    private final EventLoopGroup batchNioEventLoopGroup;

    /** The random. */
    private final Random random = new Random();

    /** The service. */
    private final ScheduledExecutorService service;

    /**
     * The Class ChannelCreator.
     * @author arung
     */
    private class ChannelCreator implements Runnable {

        /** The batch connection. */
        ConnectionPool batchConnection;
        
        /** The host. */
        private final HostConfig host;

        /** Instantiates a new channel creator.
         * @param connectionPool
         *            the connection pool
         * @param host
         *            the host
         */
        public ChannelCreator(final ConnectionPool connectionPool, final HostConfig host) {
            this.batchConnection = connectionPool;
            this.host = host;

        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            Channel outChannel = null;
            try {
                if (ConnectionPool.LOGGER.isTraceEnabled()) {
                    ConnectionPool.LOGGER.trace(String.format("Creating channel for host %s and port %s",
                            host.getHost(), host.getPort()));
                }
                final ChannelFuture f = ConnectionPool.BOOTSTRAP_MAP.get(host).connect(host.getHost(), host.getPort())
                        .sync();

                outChannel = f.channel();
                // ConnectionPool.logger.trace(TraceLogs.BATCH_CONNECTION_CHANNEL_IS + TraceLogs.COLON + outChannel);
                getAtomicBatchCounter().incrementAndGet();
                batchConnection.get(host.getHost()).add(outChannel);
                if (ConnectionPool.LOGGER.isTraceEnabled()) {
                    ConnectionPool.LOGGER.trace(String.format("Created channel %s ", outChannel.toString()));
                }
                // ConnectionPool.logger.trace(TraceLogs.PUTTING_CHANNEL + outChannel);
                outChannel.closeFuture().sync();
                if (ConnectionPool.LOGGER.isTraceEnabled()) {
                    ConnectionPool.LOGGER.trace(String.format("Shutting down channel for host %s and port %s",
                            host.getHost(), host.getPort()));
                    // ConnectionPool.logger.trace(TraceLogs.CLOSING_BATCH_CONNECTION_CHANNEL);
                }

            } catch (final InterruptedException e) {

                // ConnectionPool.logger.trace(TraceLogs.INTERRUPTED);
            } finally {
                // ConnectionPool.logger.trace(TraceLogs.CHANNEL_TERMINATED + outChannel);
                // ConnectionPool.logger.trace(TraceLogs.RETRYING_BATCH_CONNECTION);
                getAtomicBatchCounter().decrementAndGet();
                getAtomicBatchCounter().compareAndSet(-1, 0);
                if (outChannel != null) {
                    outChannel.close();
                }
                batchConnection.get(host.getHost()).remove(outChannel);

                // if (host.getSyncType() != SyncType.OPENCLOSE) { // retry has to be done by the request making thread
                batchConnection.timedConnect(host);
                // }
            }
        }
    }

    /** Instantiates a new batch connection.
     */
    private ConnectionPool() {
        batchNioEventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("HttpConnectionEVG",
                Thread.NORM_PRIORITY));
        service = Executors.newScheduledThreadPool(5, // TODO:make sure this is big enough to have all the channels --
                                                      // DONE
                new NHTTPClientThreadFactory("HttpConnectionPool"));
        ((ScheduledThreadPoolExecutor) service).setRemoveOnCancelPolicy(true);
        ConnectionPool.TOT_MAX_CONN.addAndGet(5);

    }

    /** Creates the new connection.
     * @param hostConfig
     *            the host config
     * @return the channel
     * @throws InterruptedException
     *             the interrupted exception
     */
    public Channel createNewConnection(final HostConfig hostConfig) throws InterruptedException {
        final ChannelFuture f = ConnectionPool.BOOTSTRAP_MAP.get(hostConfig)
                .connect(hostConfig.getHost(), hostConfig.getPort()).sync();

        f.channel();
        return f.channel();
    }

    /** Inits the.
     * @param host
     *            the host
     * @throws AlreadyRegisteredHostException
     *             the already registered host exception
     */

    public void registerHost(final HostConfig host) throws AlreadyRegisteredHostException {
        if (ConnectionPool.LOGGER.isTraceEnabled()) {
            ConnectionPool.LOGGER.trace(String.format("Registering host %s", host));
        }
        if (ConnectionPool.BOOTSTRAP_MAP.containsKey(host)) {
            throw new AlreadyRegisteredHostException();
        }
        final Bootstrap batchBootstrap = new Bootstrap();
        batchBootstrap.group(batchNioEventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, host.getConnectTimeOut())
                .handler(new HttpRequestIntializer());
        ConnectionPool.BOOTSTRAP_MAP.put(host, batchBootstrap);
        ConnectionPool.TOT_MAX_CONN.addAndGet(host.getTotalConnections());
        // ((ThreadPoolExecutor) service).setMaximumPoolSize(totalValNow);
        bootstrap(host);

    }

    /** Bootstrap.
     * @param host
     *            the host
     */
    public void bootstrap(final HostConfig host) {

        this.put(host.getHost(), new LinkedBlockingQueue<Channel>());

        if (host.getSyncType() != SyncType.OPENCLOSE) {
            // do this only if the connection is not open and close. If the
            // // connection is open and close then we connection creation will
            // // be initiated by the request.
            for (int i = 0; i < host.getTotalConnections(); i++) {
                timedConnect(host);
            }
        }
    }

    /**
     * Gets the atomic batch counter.
     * @return the atomic batch counter
     */
    public AtomicInteger getAtomicBatchCounter() {
        return ConnectionPool.ATOM_BATCH_COUNTER;
    }

    /** Timed connect.
     * @param host
     *            the host
     */
    private void timedConnect(final HostConfig host) {
        try {

            service.schedule(new ChannelCreator(this, host), random.nextInt(host.getRetryTime()), TimeUnit.MILLISECONDS);
        } finally {
        }

    }

    /** Cleanup.
     */
    public void cleanup() {
        if (ConnectionPool.LOGGER.isTraceEnabled()) {
            ConnectionPool.LOGGER.trace("SHUTTING DOWN NETTY ASYNC HTTP CLIENT EVENT LOOP GROUP");
        }
        batchNioEventLoopGroup.shutdownGracefully();
        if (ConnectionPool.LOGGER.isTraceEnabled()) {
            ConnectionPool.LOGGER.trace("SHUTTING DOWN CONNECTION POOL");
        }
        service.shutdown();
    }

}
