


package pns.alltypes.netty.httpclient.request;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.config.HostConfig;
import pns.alltypes.netty.httpclient.config.SyncType;
import pns.alltypes.netty.httpclient.factory.NHTTPClientThreadFactory;
import pns.alltypes.netty.httpclient.pool.ConnectionPool;
import pns.alltypes.netty.httpclient.response.ResponseCallBack;
import pns.alltypes.netty.httpclient.sync.RequestResponeSync;

/**
 * The Class HttpRequestConsumerCollection.
 * @author arung
 */
public class HttpRequestConsumerCollection {

    /** The Constant logger. */
    private static final Logger LOGGER = Logger.getLogger(ConnectionPool.class);

    /** The Constant HTTP_REQUEST_CONSUMER_COLLECTION. */
    private static final HttpRequestConsumerCollection HTTP_REQUEST_CONSUMER_COLLECTION = new HttpRequestConsumerCollection();

    /** The Constant REGISTERED_CALLBACKS. */
    private static final Map<String, ResponseCallBack> REGISTERED_CALLBACKS = new ConcurrentHashMap<String, ResponseCallBack>();

    /** The Constant CONNECTION_POOL. */
    private static final ConnectionPool CONNECTION_POOL = ConnectionPool.getInstance();

    /** The Constant HTTP_REQUEST_QUEUE_COLLECTION. */
    private static final HttpRequestQueueCollection HTTP_REQUEST_QUEUE_COLLECTION = HttpRequestQueueCollection
            .getInstance();

    /** The Constant REQUEST_RESPONE_SYNC. */
    private static final RequestResponeSync REQUEST_RESPONE_SYNC = RequestResponeSync.getInstance();

    /** The service. */
    private transient final ExecutorService httpRequestConsumerExecutorService;

    /** Instantiates a new http request consumer collection.
     */
    private HttpRequestConsumerCollection() {
        if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
            HttpRequestConsumerCollection.LOGGER.trace("inside ApnsDeliveryClientCollection");
        } 
        //increase idleness and hence copied this from cached threadpool
        //improved idleness
        httpRequestConsumerExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,5, TimeUnit.HOURS,new SynchronousQueue<Runnable>(),new NHTTPClientThreadFactory("HttpRequestConsumerCollection"));
    }

    /** Gets the single instance of HttpRequestConsumerCollection.
     * @return single instance of HttpRequestConsumerCollection
     */
    public static HttpRequestConsumerCollection getInstance() {
        return HttpRequestConsumerCollection.HTTP_REQUEST_CONSUMER_COLLECTION;
    }

    /** The Class QueueConsumer.
     */
    class QueueConsumer implements Runnable {

        /** The logger. */
        private final Logger logger = Logger.getLogger(QueueConsumer.class);
        
        /** The http request counter. */
        private final HttpRequestCounter HTTP_REQUEST_COUNTER = HttpRequestCounter.getInstance();

        /** The channel. */
        private Channel channel;
        
        /** The single message queue. */
        private final BlockingQueue<HttpRequestMessage> singleMessageQueue;
        
        /** The host config. */
        private final HostConfig hostConfig;

        /** Instantiates a new queue consumer.
         * @param hostConfig
         *            the host config
         */
        QueueConsumer(final HostConfig hostConfig) {

            this.hostConfig = hostConfig;
            singleMessageQueue = HttpRequestConsumerCollection.HTTP_REQUEST_QUEUE_COLLECTION.get(hostConfig);
            if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                logger.trace(String.format("Http Request Consumer %s created for host %s", this, hostConfig));
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {

            if (hostConfig.getSyncType() != SyncType.OPENCLOSE) {
                try {
                    channel = HttpRequestConsumerCollection.CONNECTION_POOL.get(hostConfig.getHost()).take();
                } catch (final InterruptedException e1) {
                    if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                        logger.trace("Iam interrupted");
                    }
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("Created  consumer : %d for channel %s and hostConfig  %s", this.hashCode(),
                        channel, hostConfig));
            }
            try {
                while (true) {

                    if (hostConfig.getSyncType() != SyncType.OPENCLOSE && !channel.isActive()) {
                        break;
                    }

                    HttpRequestMessage message = null;
                    try {
                        if (logger.isTraceEnabled()) {
                            logger.trace(String.format("GOING TO WAIT FOR MESSAGE ON QUEUE FROM CONSUMER %s", this));
                        }
                        message = singleMessageQueue.take();
                        if (logger.isTraceEnabled()) {
                            logger.trace(String.format("Got message %s from queue", message));
                        }

                        if (hostConfig.getSyncType() == SyncType.OPENCLOSE) {// worst since we cant do pooling
                            try {
                                channel = HttpRequestConsumerCollection.CONNECTION_POOL.createNewConnection(hostConfig);
                            } catch (final InterruptedException e) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("I am interrupted");
                                }
                            }

                            if (channel == null) {
                                singleMessageQueue.add(message);
                                break;
                            }

                        }

                        final HttpRequestMessage msg2 = message;
                        // channel.close();
                        if (message != null) {
                            if (logger.isTraceEnabled()) {
                                logger.trace(String
                                        .format("Writing to channel %s request message %s", channel, message));
                            }
                            HttpRequestConsumerCollection.REQUEST_RESPONE_SYNC.createLatch(msg2);

                            final ChannelFuture future = channel.pipeline().writeAndFlush(message);

                            future.addListener(new GenericFutureListener<Future<? super Void>>() {

                                @Override
                                public void operationComplete(final Future<? super Void> future) throws Exception {
                                    if (logger.isTraceEnabled()) {
                                        logger.trace(String.format("Current write request status %b",
                                                future.isSuccess()));
                                    }
                                    msg2.setWriteSuccess(future.isSuccess());
                                    final boolean reached = HTTP_REQUEST_COUNTER.incrementRequests(msg2.getRequestId());
                                    if (reached && !future.isSuccess()) {
                                        if (logger.isTraceEnabled()) {
                                            logger.trace(String.format("Maximum requests reached for %s", msg2));
                                        }

                                        msg2.setMaxRequestsReached(true);
                                    } else if (future.isSuccess()) {
                                        if (logger.isTraceEnabled()) {
                                            logger.trace(String.format("Written message successfully for request %s",
                                                    msg2));
                                        }

                                    } else if (!reached) {
                                        if (logger.isTraceEnabled()) {
                                            logger.trace(String.format("Retrying request %s since it failed", msg2));
                                            logger.trace(String.format("Cause for failure %s  IsCancelled: %s IsDone:%s", future.cause(), future.isCancelled(), future.isDone()));
                                        }
                                        HttpRequestConsumerCollection.REQUEST_RESPONE_SYNC.resume(msg2);
                                    }

                                }
                            });
                            // future.awaitUninterruptibly(hostConfig.getReadTimeout());

                        }

                    } catch (final Throwable e) {
                        if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                            HttpRequestConsumerCollection.LOGGER.trace(String.format(
                                    "Exception occurred while making request %s", message));
                        }
                    } finally {
                        if (message != null) {
                            if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                                logger.trace(String.format("Going to sync on request response latch for message %s",
                                        message));
                            }
                            HttpRequestConsumerCollection.REQUEST_RESPONE_SYNC.sync(message);
                            if (logger.isTraceEnabled()) {
                                logger.trace(String.format("Got out of  sync on request response latch for message %s",
                                        message));
                            }
                            HttpRequestConsumerCollection.REQUEST_RESPONE_SYNC.removeRequestLatch(message);
                            final ResponseCallBack registeredCallback = getRegisteredCallback(message);
                            if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                                HttpRequestConsumerCollection.LOGGER.trace(String.format(
                                        "Calling callable %s with write statuss %b", registeredCallback,
                                        message.isWriteSuccess()));
                            }
                            if (registeredCallback != null) {
                                registeredCallback.httpResponse(
                                        HttpRequestConsumerCollection.REQUEST_RESPONE_SYNC.consumeResponse(message),
                                        message.isWriteSuccess());
                                removeRegisteredCallback(message);
                            }

                            if (message.isWriteSuccess() || message.isMaxRequestsReached()) {
                                if (logger.isTraceEnabled()) {
                                    if (message.isWriteSuccess()) {
                                        logger.trace(String.format(
                                                "Write is successfull and hence resetting counter for request %s",
                                                message));
                                    } else if (message.isMaxRequestsReached()) {
                                        logger.trace(String.format(
                                                "Resetting request counter for message %s since max requests reached ",
                                                message));
                                    }
                                }

                                HTTP_REQUEST_COUNTER.removeRequests(message.getRequestId());
                                if (hostConfig.getSyncType() == SyncType.OPENCLOSE) {
                                    channel.close();
                                }
                            }

                            if (!message.isWriteSuccess() && !message.isMaxRequestsReached()) {
                                if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                                    HttpRequestConsumerCollection.LOGGER.trace(String.format(
                                            "-- READDING MESSAGE %s TO QUEUE SINCE REQUEST FAILED --", message));
                                }
                                singleMessageQueue.add(message);
                            }
                        }
                    }

                    if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                        HttpRequestConsumerCollection.LOGGER.trace(String.format(
                                "-- REQUEST PROCESSING COMPLETED FOR REQUEST -- %s", message));
                    }

                }

            } finally {

                if (hostConfig.getSyncType() != SyncType.OPENCLOSE) {
                    if (channel != null) {
                        try {
                            if (logger.isTraceEnabled()) {
                                logger.trace(String.format("Closing the channel %s", channel));
                            }
                            channel.close().sync();
                        } catch (final InterruptedException e) {
                            // wait intil close is complete.
                        }
                    }

                    try {
                        if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                            HttpRequestConsumerCollection.LOGGER.trace(String.format("CONSUMER CREATION FOR HOST %s SCHEDULED",
                                    hostConfig));
                        }
                        httpRequestConsumerExecutorService.submit(new QueueConsumer(hostConfig));
                  
                    } finally {
                    }

                }

            }
        }
    }

    /** Gets the registered callback.
     * @param httpRequestMessage
     *            the http request message
     * @return the registered callback
     */
    public ResponseCallBack getRegisteredCallback(final HttpRequestMessage httpRequestMessage) {
        return HttpRequestConsumerCollection.REGISTERED_CALLBACKS.get(getRegCallbackKey(httpRequestMessage));
    }

    /** Removes the registered callback.
     * @param httpRequestMessage
     *            the http request message
     */
    public void removeRegisteredCallback(final HttpRequestMessage httpRequestMessage) {
        HttpRequestConsumerCollection.REGISTERED_CALLBACKS.remove(getRegCallbackKey(httpRequestMessage));
    }

    /** Gets the reg callback key.
     * @param httpRequestMessage
     *            the http request message
     * @return the reg callback key
     */
    private String getRegCallbackKey(final HttpRequestMessage httpRequestMessage) {
        return httpRequestMessage.getRequestId();
    }

    /** Adds the consumer.
     * @param hostConfig
     *            the host config
     */
    public void addConsumer(final HostConfig hostConfig) {
        try {
            if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
                HttpRequestConsumerCollection.LOGGER.trace(String.format("CONSUMER CREATION FOR HOST %s SCHEDULED",
                        hostConfig));
            }
            httpRequestConsumerExecutorService.execute(new QueueConsumer(hostConfig));
      
        } finally {
        }
    }

    /** Update registered callback.
     * @param httpRequestMessage
     *            the http request message
     * @param callback
     *            the callback
     */
    public void updateRegisteredCallback(final HttpRequestMessage httpRequestMessage, final ResponseCallBack callback) {
        HttpRequestConsumerCollection.REGISTERED_CALLBACKS.put(getRegCallbackKey(httpRequestMessage), callback);

    }

    /** Cleanup.
     */
    public void cleanup() {
        if (HttpRequestConsumerCollection.LOGGER.isTraceEnabled()) {
            HttpRequestConsumerCollection.LOGGER.trace("SHUTTING DOWN HTTPREQUEST CONSUMER COLLECTION");
        }
        httpRequestConsumerExecutorService.shutdown();
    }
}
