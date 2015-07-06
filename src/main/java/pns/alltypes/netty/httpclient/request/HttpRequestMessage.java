


package pns.alltypes.netty.httpclient.request;

import pns.alltypes.netty.httpclient.response.ResponseCallBack;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * The Class HttpRequestMessage.
 * @author arung
 */
public class HttpRequestMessage extends AbstractRequestMessage {

    /** The url. */
    private String url;
    
    /** The secured. */
    private boolean secured = false;
    
    /** The http method. */
    private HttpMethod httpMethod;
    
    /** The response callback. */
    private ResponseCallBack responseCallback;
    
    /** The headers. */
    private final DefaultHttpHeaders headers;
    
    /** The body. */
    private String body;
    
    /** The write success. */
    private volatile boolean writeSuccess;
    
    /** The max requests reached. */
    private volatile boolean maxRequestsReached;

    /** Instantiates a new http request message.
     * @param builder
     *            the builder
     */
    public HttpRequestMessage(final Builder builder) {
        this.url = builder.url;
        this.httpMethod = builder.httpMethod;
        this.responseCallback = builder.responseCallBack;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    /** The Class Builder.
     */
    public static class Builder {
        
        /** The body. */
        public String body;
        
        /** The response call back. */
        private ResponseCallBack responseCallBack;
        
        /** The http method. */
        private HttpMethod httpMethod;
        
        /** The headers. */
        private DefaultHttpHeaders headers;
        
        /** The url. */
        private String url;
        
        /** The url template. */
        private String urlTemplate;

        /** Instantiates a new builder.
         */
        Builder() {

        }

        /** Url template.
         * @param urlTemplate
         *            the url template
         * @return the builder
         */
        public Builder urlTemplate(final String urlTemplate) {
            this.urlTemplate = urlTemplate;
            return this;
        }

        /** Url.
         * @return the builder
         */
        public Builder url() {
            this.url = new String(urlTemplate);
            return this;

        }

        /** Adds the url param.
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the builder
         */
        public Builder addUrlParam(final String name, final String value) {
            // TODO Auto-generated method stub
            this.url = this.url.replace(name, value);
            return this;
        }

        /** Adds the url param.
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the builder
         */
        public Builder addUrlParam(final String name, final int value) {
            this.url = this.url.replace(name, String.valueOf(value));
            return this;

        }

        /** Response callback.
         * @param responseCallBack
         *            the response call back
         * @return the builder
         */
        public Builder responseCallback(final ResponseCallBack responseCallBack) {
            this.responseCallBack = responseCallBack;
            return this;

        }

        /** Http method.
         * @param httpMethod
         *            the http method
         * @return the builder
         */
        public Builder httpMethod(final HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        /** Headers.
         * @return the builder
         */
        public Builder headers() {
            headers = new DefaultHttpHeaders();
            return this;
        }

        /** Adds the header.
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the builder
         */
        public Builder addHeader(final String name, final String value) {
            headers.add(name, value);
            return this;
        }

        /** Body.
         * @param body
         *            the body
         * @return the builder
         */
        public Builder body(final String body) {

            this.body = body;
            return this;
        }

        /** Builds the.
         * @return the http request message
         */
        public HttpRequestMessage build() {
            return new HttpRequestMessage(this);
        }

    }

    /** Gets the url.
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /** Sets the url.
     * @param url
     *            the url to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /** Checks if is secured.
     * @return the secured
     */
    public boolean isSecured() {
        return this.secured;
    }

    /** Sets the secured.
     * @param secured
     *            the secured to set
     */
    public void setSecured(final boolean secured) {
        this.secured = secured;
    }

    /** Gets the http method.
     * @return the httpMethod
     */
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    /** Sets the http method.
     * @param httpMethod
     *            the httpMethod to set
     */
    public void setHttpMethod(final HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    /** Gets the response callback.
     * @return the responseCallback
     */
    public ResponseCallBack getResponseCallback() {
        return this.responseCallback;
    }

    /** Sets the response callback.
     * @param responseCallback
     *            the responseCallback to set
     */
    public void setResponseCallback(final ResponseCallBack responseCallback) {
        this.responseCallback = responseCallback;
    }

    /** Adds the header.
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void addHeader(final String name, final String value) {
        getHeaders().add(name, value);
    }

    /** Gets the headers.
     * @return the headers
     */
    public DefaultHttpHeaders getHeaders() {
        return headers;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("[ id: %s , url: %s, method : %s ,body: %s] ", requestId,
                url.substring(0, 10).concat("..."), httpMethod, getBody());
    }

    /** Gets the body.
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /** Sets the body.
     * @param body
     *            the new body
     */
    public void setBody(final String body) {
        this.body = body;
    }

    /** Checks if is write success.
     * @return true, if is write success
     */
    public boolean isWriteSuccess() {
        // TODO Auto-generated method stub
        return writeSuccess;
    }

    /** Sets the write success.
     * @param writeSuccess
     *            the new write success
     */
    public void setWriteSuccess(final boolean writeSuccess) {
        this.writeSuccess = writeSuccess;
    }

    /** Checks if is max requests reached.
     * @return true, if is max requests reached
     */
    public boolean isMaxRequestsReached() {
        return maxRequestsReached;
    }

    /** Sets the max requests reached.
     * @param maxRequestsReached
     *            the new max requests reached
     */
    public void setMaxRequestsReached(final boolean maxRequestsReached) {
        this.maxRequestsReached = maxRequestsReached;
    }
}
