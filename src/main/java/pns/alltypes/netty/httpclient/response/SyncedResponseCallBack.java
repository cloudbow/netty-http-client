/**
 *
 */
package pns.alltypes.netty.httpclient.response;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

/**
 * The Class SyncedResponseCallBack.
 * @author arung
 */
public class SyncedResponseCallBack implements ResponseCallBack {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(SyncedResponseCallBack.class);

    /** The response. */
    private volatile ResponseMsg response;
    
    /** The count down latch. */
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    /* (non-Javadoc)
     * @see com.slingmedia.common.asynchttpclient.ResponseCallBack#httpResponse(com.slingmedia.common.asynchttpclient.ResponseMsg)
     */
    @Override
    public void httpResponse(final ResponseMsg response, final boolean writeSuccess) {

        this.response = response;

        if (writeSuccess) {
            if (SyncedResponseCallBack.LOGGER.isTraceEnabled()) {
                SyncedResponseCallBack.LOGGER.trace(String.format("Counting down on latch %s with response %s",
                        countDownLatch, response.getResponse()));
            }
            getCountDownLatch().countDown();
        }
    }

    /** Gets the response.
     * @return the response
     */
    public ResponseMsg getResponse() {
        return response;
    }

    /** Gets the count down latch.
     * @return the count down latch
     */
    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    /* (non-Javadoc)
     * @see com.slingmedia.common.asynchttpclient.response.ResponseCallBack#httpResponse(com.slingmedia.common.asynchttpclient.response.ResponseMsg)
     */
    @Override
    public void httpResponse(final ResponseMsg response) {
        // TODO Auto-generated method stub

    }

}
