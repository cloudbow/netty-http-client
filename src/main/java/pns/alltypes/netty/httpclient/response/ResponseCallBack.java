/**
 *
 */
package pns.alltypes.netty.httpclient.response;

/**
 * The Interface ResponseCallBack.
 * @author arung
 */
public interface ResponseCallBack {
    
    /** Http response.
     * @param response
     *            the response
     * @param writeSuccess
     *            the write success
     */
    void httpResponse(ResponseMsg response, boolean writeSuccess);

    /** Http response.
     * @param response
     *            the response
     */
    void httpResponse(ResponseMsg response);
}
