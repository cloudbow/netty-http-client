/**
 *
 */
package pns.alltypes.netty.httpclient.response;

/**
 * The Interface ExternalCallback.
 * @author arung
 */
public interface ExternalCallback {
    
    /** Http response.
     * @param response
     *            the response
     */
    void httpResponse(ResponseMsg response);
}
