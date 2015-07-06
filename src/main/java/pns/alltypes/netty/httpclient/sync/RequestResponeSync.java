


package pns.alltypes.netty.httpclient.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.request.HttpRequestMessage;
import pns.alltypes.netty.httpclient.request.RequestMessage;
import pns.alltypes.netty.httpclient.response.ResponseMsg;

/**
 * The Class RequestResponeSync.
 * @author arung
 */

public class RequestResponeSync {

    /** The Constant MAX_REQ_TIMEOUT. */
    private static final int MAX_REQ_TIMEOUT = 12 * 1000;
    
    /** The Constant REQ_LATCH_MAP. */
    private static final Map<String, CountDownLatch> REQ_LATCH_MAP = new ConcurrentHashMap<String, CountDownLatch>(1000);
    
    /** The Constant RESP_MAP. */
    private static final Map<String, ResponseMsg> RESP_MAP = new ConcurrentHashMap<String, ResponseMsg>(1000);
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(RequestResponeSync.class);

    /** The Constant REQUEST_RESPONE_SYNC. */
    private static final RequestResponeSync REQUEST_RESPONE_SYNC = new RequestResponeSync();

    /** Instantiates a new request respone sync.
     */
    private RequestResponeSync() {

    }

    /** Gets the single instance of RequestResponeSync.
     * @return single instance of RequestResponeSync
     */
    public static RequestResponeSync getInstance() {

        return RequestResponeSync.REQUEST_RESPONE_SYNC;
    }

    /** Creates the latch.
     * @param msg2
     *            the msg2
     */
    public void createLatch(final HttpRequestMessage msg2) {
        final CountDownLatch requestLatch = new CountDownLatch(1);
        if (RequestResponeSync.LOGGER.isTraceEnabled()) {
            RequestResponeSync.LOGGER.trace(String.format("Creating latch for request id %s", msg2.getRequestId()));
        }
        RequestResponeSync.REQ_LATCH_MAP.put(msg2.getRequestId(), requestLatch);

    }

    /** Sync.
     * @param rq
     *            the rq
     */
    public void sync(final RequestMessage rq) {

        final CountDownLatch requestLatch = RequestResponeSync.REQ_LATCH_MAP.get(rq.getRequestId());
        if (RequestResponeSync.LOGGER.isTraceEnabled()) {
            RequestResponeSync.LOGGER.trace(String.format("Going to wait on request latch %s with request id %s",
                    requestLatch, rq.getRequestId()));
        }
        boolean countDownLatchSuccess = false;
        try {
            countDownLatchSuccess = requestLatch.await(RequestResponeSync.MAX_REQ_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            if (RequestResponeSync.LOGGER.isTraceEnabled()) {
                RequestResponeSync.LOGGER.trace("Interrupted");
            }
        } finally {
            if (!countDownLatchSuccess) {
                if (RequestResponeSync.LOGGER.isTraceEnabled()) {
                    RequestResponeSync.LOGGER.trace(String.format(
                            "Request latch for message %s waited for response max time and timeout!!!", rq));
                }
                resume(rq);
            } else {
                if (RequestResponeSync.LOGGER.isTraceEnabled()) {
                    RequestResponeSync.LOGGER.trace(String.format(
                            "Request latch for message %s counted down due to latch timeout", rq));
                }

            }

        }

    }

    /** Removes the request latch.
     * @param rq
     *            the rq
     */
    public void removeRequestLatch(final RequestMessage rq) {
        if (RequestResponeSync.LOGGER.isTraceEnabled()) {
            RequestResponeSync.LOGGER.trace(String.format("Removing latch  from map for request id %s:",
                    rq.getRequestId()));
        }
        RequestResponeSync.REQ_LATCH_MAP.remove(rq.getRequestId());
    }

    /** Resume.
     * @param rq
     *            the rq
     */
    public void resume(final RequestMessage rq) {
        final CountDownLatch countDownLatch = RequestResponeSync.REQ_LATCH_MAP.get(rq.getRequestId());
        if (countDownLatch != null) {
            if (RequestResponeSync.LOGGER.isTraceEnabled()) {
                RequestResponeSync.LOGGER.trace(String.format("Counting down request latch on resume for request %s",
                        rq));
            }
            countDownLatch.countDown();
        }

    }

    /** Put response.
     * @param rq
     *            the rq
     * @param response
     *            the response
     */
    public void putResponse(final RequestMessage rq, final ResponseMsg response) {
        RequestResponeSync.RESP_MAP.put(rq.getRequestId(), response);

    }

    /** Consume response.
     * @param rq
     *            the rq
     * @return the response msg
     */
    public ResponseMsg consumeResponse(final RequestMessage rq) {
        final ResponseMsg message = RequestResponeSync.RESP_MAP.remove(rq.getRequestId());
        if (RequestResponeSync.LOGGER.isTraceEnabled()) {
            RequestResponeSync.LOGGER.trace(String.format("Consuming message %s for request %s", message, rq));
        }

        return message;
    }

}
