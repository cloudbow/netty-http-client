/**
 *
 */
package pns.alltypes.netty.httpclient.request;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.response.ExternalCallback;
import pns.alltypes.netty.httpclient.response.ResponseCallBack;
import pns.alltypes.netty.httpclient.response.ResponseMsg;

/**
 * The Class AsyncResponseCallBack.
 * @author arung
 */
public class AsyncResponseCallBack implements ResponseCallBack {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(AsyncResponseCallBack.class);

    /** The response. */
    private volatile ResponseMsg response;

    /** The count down latch. */
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    /** The caller response. */
    private final ExternalCallback callerResponse;

    /**
     * Instantiates a new async response call back.
     * @param responseCallBack
     *            the response call back
     */
    public AsyncResponseCallBack(final ExternalCallback responseCallBack) {
        this.callerResponse = responseCallBack;

    }

    @Override
    public void httpResponse(final ResponseMsg response, final boolean writeSuccess) {
        if (AsyncResponseCallBack.LOGGER.isTraceEnabled()) {
            AsyncResponseCallBack.LOGGER.trace(String.format("Setting response %s in  callback", response));
        }
        this.response = response;

        if (writeSuccess) {
            if (AsyncResponseCallBack.LOGGER.isTraceEnabled()) {
                AsyncResponseCallBack.LOGGER.trace(String.format("Counting down on latch %s with response %s", countDownLatch, response));
            }
            getCountDownLatch().countDown();
            callerResponse.httpResponse(response);

        }
    }

    /**
     * Gets the response.
     * @return the response
     */
    public ResponseMsg getResponse() {
        return response;
    }

    /**
     * Gets the count down latch.
     * @return the count down latch
     */
    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    @Override
    public void httpResponse(final ResponseMsg response) {
        // TODO Auto-generated method stub

    }

}
