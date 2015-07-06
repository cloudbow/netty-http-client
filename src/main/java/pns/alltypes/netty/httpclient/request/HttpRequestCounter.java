/**
 *
 */
package pns.alltypes.netty.httpclient.request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class HttpRequestCounter.
 * @author arung
 */
public class HttpRequestCounter extends ConcurrentHashMap<String, AtomicInteger> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant MAX_REQUESTS_LIMIT. */
    private static final int MAX_REQUESTS_LIMIT = 4;

    /** The Constant HTTP_REQUEST_COUNTER. */
    private static final HttpRequestCounter HTTP_REQUEST_COUNTER = new HttpRequestCounter();

    /** Instantiates a new http request counter.
     */
    private HttpRequestCounter() {

    }

    /** Gets the single instance of HttpRequestCounter.
     * @return single instance of HttpRequestCounter
     */
    public static HttpRequestCounter getInstance() {
        return HttpRequestCounter.HTTP_REQUEST_COUNTER;
    }

    /** Increment requests.
     * @param requestId
     *            the request id
     * @return true, if successful
     */
    public boolean incrementRequests(final String requestId) {
        if (HttpRequestCounter.HTTP_REQUEST_COUNTER.get(requestId) == null) {
            HttpRequestCounter.HTTP_REQUEST_COUNTER.put(requestId, new AtomicInteger(0));
        }
        final int value = HttpRequestCounter.HTTP_REQUEST_COUNTER.get(requestId).incrementAndGet();
        if (value == HttpRequestCounter.MAX_REQUESTS_LIMIT) {
            HttpRequestCounter.HTTP_REQUEST_COUNTER.remove(requestId);
            return true;
        }
        return false;
    }

    /** Removes the requests.
     * @param requestId
     *            the request id
     */
    public void removeRequests(final String requestId) {
        HttpRequestCounter.HTTP_REQUEST_COUNTER.remove(requestId);
    }

}
