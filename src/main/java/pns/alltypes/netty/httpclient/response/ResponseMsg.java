/**
 *
 */
package pns.alltypes.netty.httpclient.response;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * The Class ResponseMsg.
 * @author arung
 */
public class ResponseMsg {

    /** The response. */
    private String response;
    
    /** The http response status. */
    private HttpResponseStatus httpResponseStatus;

    /** Instantiates a new response msg.
     * @param content
     *            the content
     * @param status
     *            the status
     */
    public ResponseMsg(final String content, final HttpResponseStatus status) {
        this.response = content;
        // TODO Auto-generated constructor stub
        this.httpResponseStatus = status;
    }

    /** Gets the response.
     * @return the response
     */
    public String getResponse() {
        return this.response;
    }

    /** Sets the response.
     * @param response
     *            the response to set
     */
    public void setResponse(final String response) {
        this.response = response;
    }

    /** Gets the http response status.
     * @return the httpResponseStatus
     */
    public HttpResponseStatus getHttpResponseStatus() {
        return this.httpResponseStatus;
    }

    /** Sets the http response status.
     * @param httpResponseStatus
     *            the httpResponseStatus to set
     */
    public void setHttpResponseStatus(final HttpResponseStatus httpResponseStatus) {
        this.httpResponseStatus = httpResponseStatus;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return String.format("[Response : %s,status: %s]", response, httpResponseStatus);
    }

}
