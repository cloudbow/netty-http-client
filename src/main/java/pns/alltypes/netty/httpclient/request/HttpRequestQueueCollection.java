/**
 *
 */
package pns.alltypes.netty.httpclient.request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import pns.alltypes.netty.httpclient.config.HostConfig;
import pns.alltypes.netty.httpclient.exception.AlreadyRegisteredHostException;

/**
 * The Class HttpRequestQueueCollection.
 * @author arung
 */
public class HttpRequestQueueCollection extends ConcurrentHashMap<HostConfig, BlockingQueue<HttpRequestMessage>> {

    /** The Constant HTTP_REQUEST_QUEUE_COLLECTION2. */
    private static final HttpRequestQueueCollection HTTP_REQUEST_QUEUE_COLLECTION2 = new HttpRequestQueueCollection();

    /** Instantiates a new http request queue collection.
     */
    private HttpRequestQueueCollection() {

    }

    /** Gets the single instance of HttpRequestQueueCollection.
     * @return single instance of HttpRequestQueueCollection
     */
    public static HttpRequestQueueCollection getInstance() {
        return HttpRequestQueueCollection.HTTP_REQUEST_QUEUE_COLLECTION2;
    }

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Map empty queue for host.
     * @param hostConfig
     *            the host config
     * @throws AlreadyRegisteredHostException
     *             the already registered host exception
     */
    public void mapEmptyQueueForHost(final HostConfig hostConfig) throws AlreadyRegisteredHostException {

        if (HttpRequestQueueCollection.HTTP_REQUEST_QUEUE_COLLECTION2.get(hostConfig) != null) {
            throw new AlreadyRegisteredHostException();
        }

        HttpRequestQueueCollection.HTTP_REQUEST_QUEUE_COLLECTION2.put(hostConfig,
                new LinkedBlockingDeque<HttpRequestMessage>());
    }

}
