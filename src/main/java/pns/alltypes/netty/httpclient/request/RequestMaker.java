/**
 *
 */
package pns.alltypes.netty.httpclient.request;

import io.netty.handler.codec.http.HttpMethod;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.config.HostConfig;
import pns.alltypes.netty.httpclient.exception.AlreadyRegisteredHostException;
import pns.alltypes.netty.httpclient.exception.InvalidResponseException;
import pns.alltypes.netty.httpclient.factory.NHTTPClientThreadFactory;
import pns.alltypes.netty.httpclient.pool.ConnectionPool;
import pns.alltypes.netty.httpclient.response.ExternalCallback;
import pns.alltypes.netty.httpclient.response.ResponseCallBack;
import pns.alltypes.netty.httpclient.response.ResponseMsg;
import pns.alltypes.netty.httpclient.response.SyncedResponseCallBack;

/**
 * The Class RequestMaker.
 * @author arung
 */
public class RequestMaker {
    
    /** The Constant MAX_BLOCKING_THREADS. */
    private static final int MAX_BLOCKING_THREADS = 5;
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(RequestMaker.class);
    
    /** The Constant REQUEST_MAKER. */
    private static final RequestMaker REQUEST_MAKER = new RequestMaker();

    /** The Constant HTTP_REQUEST_QUEUE_COLLECTION. */
    private static final HttpRequestQueueCollection HTTP_REQUEST_QUEUE_COLLECTION = HttpRequestQueueCollection.getInstance();
    
    /** The Constant HTTP_REQUEST_CONSUMER_COLLECTION. */
    private static final HttpRequestConsumerCollection HTTP_REQUEST_CONSUMER_COLLECTION = HttpRequestConsumerCollection.getInstance();

    /** The Constant CONNECTION_POOL. */
    private static final ConnectionPool CONNECTION_POOL = ConnectionPool.getInstance();

    /** The Constant httpBlockingReadThreadsExecutor. */
    private static final ExecutorService httpBlockingReadThreadsExecutor = Executors.newFixedThreadPool(RequestMaker.MAX_BLOCKING_THREADS,
            new NHTTPClientThreadFactory("HttpBlockingReadThreads"));

    /** Instantiates a new request maker.
     */
    private RequestMaker() {
        RequestMaker.LOGGER.trace("Creating singleton");
    }

    /** Gets the single instance of RequestMaker.
     * @return single instance of RequestMaker
     */
    public static RequestMaker getInstance() {
        return RequestMaker.REQUEST_MAKER;
    }

    static {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                if (RequestMaker.LOGGER.isTraceEnabled()) {
                    RequestMaker.LOGGER.trace("-- CLEANUP FOR ASYNC HTTP CLIENT STARTED --");
                }
                HttpRequestConsumerCollection.getInstance().cleanup();
                RequestMaker.getInstance().cleanup();
                ConnectionPool.getInstance().cleanup();

            }

        }));

    }

    /** Configure.
     * @param maxBlockingThreads
     *            the max blocking threads
     */
    public void configure(final int maxBlockingThreads) {

        synchronized (RequestMaker.REQUEST_MAKER) {
            if (maxBlockingThreads > RequestMaker.MAX_BLOCKING_THREADS) {
                ((ThreadPoolExecutor) RequestMaker.httpBlockingReadThreadsExecutor).setMaximumPoolSize(maxBlockingThreads);
                ((ThreadPoolExecutor) RequestMaker.httpBlockingReadThreadsExecutor).setCorePoolSize(maxBlockingThreads);

            }

        }

    }

    /** Register host.
     * @param hostConfig
     *            the host config
     * @throws AlreadyRegisteredHostException
     *             the already registered host exception
     */
    public void registerHost(final HostConfig hostConfig) throws AlreadyRegisteredHostException {
        RequestMaker.HTTP_REQUEST_QUEUE_COLLECTION.mapEmptyQueueForHost(hostConfig);
        RequestMaker.CONNECTION_POOL.registerHost(hostConfig);
        final int totalConsumers = hostConfig.getTotalConsumers();
        for (int i = 0; i < totalConsumers; i++) {
            if (RequestMaker.LOGGER.isTraceEnabled()) {
                RequestMaker.LOGGER.trace(String.format("Adding consumer for hostConfig %s", hostConfig));
            }
            RequestMaker.HTTP_REQUEST_CONSUMER_COLLECTION.addConsumer(hostConfig);
        }
    }

    /** Register call back.
     * @param httpMethod
     *            the http method
     * @param urlTemplate
     *            the url template
     * @return the http request message. builder
     */
    public HttpRequestMessage.Builder registerCallBack(final HttpMethod httpMethod, final String urlTemplate) {

        final HttpRequestMessage.Builder builder = new HttpRequestMessage.Builder();
        builder.httpMethod(httpMethod).urlTemplate(urlTemplate);

        return builder;

    }

    /** Request.
     * @param httpRequestMessage
     *            the http request message
     * @param hostConfig
     *            the host config
     * @param callback
     *            the callback
     * @throws InterruptedException
     *             the interrupted exception
     */
    private void request(final HttpRequestMessage httpRequestMessage, final HostConfig hostConfig, final ResponseCallBack callback) throws InterruptedException {
        if (RequestMaker.LOGGER.isTraceEnabled()) {
            RequestMaker.LOGGER.trace(String.format("Registering callback %s for hostConfig %s", callback, hostConfig));
        }
        RequestMaker.HTTP_REQUEST_CONSUMER_COLLECTION.updateRegisteredCallback(httpRequestMessage, callback);
        RequestMaker.HTTP_REQUEST_QUEUE_COLLECTION.get(hostConfig).put(httpRequestMessage);
        if (RequestMaker.LOGGER.isTraceEnabled()) {

            RequestMaker.LOGGER.trace(String.format("Pushed request message %s to queue %s", httpRequestMessage,
                    RequestMaker.HTTP_REQUEST_QUEUE_COLLECTION.get(hostConfig)));
        }
    }

    /** Request sync.
     * @param httpRequestMessage
     *            the http request message
     * @param hostConfig
     *            the host config
     * @return the response msg
     * @throws InvalidResponseException
     *             the invalid response exception
     */
    public ResponseMsg requestSync(final HttpRequestMessage httpRequestMessage, final HostConfig hostConfig) throws InvalidResponseException {
        if (RequestMaker.LOGGER.isTraceEnabled()) {
            RequestMaker.LOGGER.trace(String.format("---- STARTING REQUEST  %s ----", httpRequestMessage));
        }
        final SyncedResponseCallBack responseCallBack = new SyncedResponseCallBack();
        final Future<ResponseMsg> responseMsg = RequestMaker.httpBlockingReadThreadsExecutor.submit(new Callable<ResponseMsg>() {

            @Override
            public ResponseMsg call() throws Exception {
                
                //call original request method
                request(httpRequestMessage, hostConfig, responseCallBack);
                
                if (RequestMaker.LOGGER.isTraceEnabled()) {
                    RequestMaker.LOGGER.trace(String.format("Syncing on lock %s for request  %s", responseCallBack.getCountDownLatch(), httpRequestMessage));
                }
                final boolean success = responseCallBack.getCountDownLatch().await(hostConfig.getRequestAttemptTimeout(), TimeUnit.MILLISECONDS);
                if (RequestMaker.LOGGER.isTraceEnabled()) {
                    if (!success) {
                        RequestMaker.LOGGER.trace(String.format("A read timeout occurred for request %s after %d !!", httpRequestMessage,
                                hostConfig.getReadTimeout()));
                    } else {
                        RequestMaker.LOGGER.trace(String.format("Request %s successfull", httpRequestMessage));
                    }
                }
                if (RequestMaker.LOGGER.isTraceEnabled()) {
                    RequestMaker.LOGGER.trace("END of outer thread");
                }
                return responseCallBack.getResponse();
            }
        });

        try {
            final ResponseMsg res = responseMsg.get();
            if (RequestMaker.LOGGER.isTraceEnabled()) {
                RequestMaker.LOGGER.trace(String.format("Going to return the response %s", res));
            }
            return res;
        } catch (InterruptedException | ExecutionException e) {
            throw new InvalidResponseException();
        }

    }

    /** Request a sync.
     * @param httpRequestMessage
     *            the http request message
     * @param hostConfig
     *            the host config
     * @param responseCallBack
     *            the response call back
     * @throws InvalidResponseException
     *             the invalid response exception
     */
    public void requestASync(final HttpRequestMessage httpRequestMessage, final HostConfig hostConfig, final ExternalCallback responseCallBack)
            throws InvalidResponseException {
        final AsyncResponseCallBack asyncRespCallback = new AsyncResponseCallBack(responseCallBack);
        RequestMaker.httpBlockingReadThreadsExecutor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    request(httpRequestMessage, hostConfig, asyncRespCallback);
                } catch (final InterruptedException e) {
                    if (RequestMaker.LOGGER.isTraceEnabled()) {
                        RequestMaker.LOGGER.trace("interrupted");
                    }

                }
                if (RequestMaker.LOGGER.isTraceEnabled()) {
                    RequestMaker.LOGGER.trace(String.format("Syncing on lock %s ", asyncRespCallback.getCountDownLatch()));
                }
                boolean success = false;
                try {
                    success = asyncRespCallback.getCountDownLatch().await(hostConfig.getRequestAttemptTimeout(), TimeUnit.MILLISECONDS);
                } catch (final InterruptedException e) {
                    if (RequestMaker.LOGGER.isTraceEnabled()) {
                        RequestMaker.LOGGER.trace("interrupted");
                    }
                }
                if (RequestMaker.LOGGER.isTraceEnabled()) {
                    if (!success) {
                        RequestMaker.LOGGER.trace(String.format("A read timeout occurred for request %s after %d !!", httpRequestMessage,
                                hostConfig.getReadTimeout()));
                    } else {
                        RequestMaker.LOGGER.trace(String.format("Request %s successfull", httpRequestMessage));
                    }
                }

            }
        });

    }

    /** Cleanup.
     */
    public void cleanup() {
        if (RequestMaker.LOGGER.isTraceEnabled()) {
            RequestMaker.LOGGER.trace("SHUTTING DOWN EXECUTOR FOR BLOCKING CONNCETIONS");
        }
        RequestMaker.httpBlockingReadThreadsExecutor.shutdown();
    }

}
