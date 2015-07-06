/**
 *
 */
package pns.alltypes.netty.httpclient;

import io.netty.handler.codec.http.HttpMethod;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.config.HostConfig;
import pns.alltypes.netty.httpclient.config.SyncType;
import pns.alltypes.netty.httpclient.exception.AlreadyRegisteredHostException;
import pns.alltypes.netty.httpclient.exception.InvalidResponseException;
import pns.alltypes.netty.httpclient.request.HttpRequestMessage.Builder;
import pns.alltypes.netty.httpclient.request.RequestMaker;
import pns.alltypes.netty.httpclient.response.ResponseMsg;

// TODO: Auto-generated Javadoc
/**
 * The Class SyncHttpClient.
 * @author arung
 */
public class SyncHttpClient {

    /** The Constant REQUEST_MAKER. */
    private static final RequestMaker REQUEST_MAKER = RequestMaker.getInstance();

    /** The Constant SYNC_HTTP_CLIENT. */
    private static final SyncHttpClient SYNC_HTTP_CLIENT = new SyncHttpClient();

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(SyncHttpClient.class);

    /**
     * The main method.
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        LogManager.getRootLogger().setLevel(Level.TRACE);
        SyncHttpClient.SYNC_HTTP_CLIENT.sendRequest();

    }

    /**
     * Send request.
     */
    private void sendRequest() {
        HostConfig hostConfig = null;
        try {
            hostConfig = new HostConfig("google.com", 80, SyncType.OPENCLOSE);
            SyncHttpClient.REQUEST_MAKER.registerHost(hostConfig);
        } catch (final AlreadyRegisteredHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final Builder registerCallBack = SyncHttpClient.REQUEST_MAKER.registerCallBack(HttpMethod.GET, "http://google.com?q=netty-http-client");
        registerCallBack.url().headers();
        ResponseMsg requestSync = null;
        try {
            requestSync = SyncHttpClient.REQUEST_MAKER.requestSync(registerCallBack.build(), hostConfig);
        } catch (final InvalidResponseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (SyncHttpClient.LOGGER.isTraceEnabled()) {
            SyncHttpClient.LOGGER.trace(requestSync.getResponse());
        }

    }

}
