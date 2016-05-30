/**
 *
 */
package pns.alltypes.netty.httpclient;

import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

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
        SyncHttpClient.SYNC_HTTP_CLIENT.sendRequests();

    }

    /**
     * Send request.
     */
   @SuppressWarnings("unused")
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
    
    private void sendRequests() {
        List<HostConfig> RUBENS_HOST_CONFIGS = new ArrayList<>();

       String[] rubensHost = new String[] {"google.com","yahoo.com","rediff.com","amazon.com","ebay.com","flipkart.com","yahoo.co.in","ebay.in","amazon.in"};
       for (String host : rubensHost) {
         HostConfig hostConfig = new HostConfig(host, 80, SyncType.OPENCLOSE);
         RUBENS_HOST_CONFIGS.add(hostConfig);
         try {
            REQUEST_MAKER.registerHost(hostConfig);
         }
         catch (AlreadyRegisteredHostException e) {
            // do nothing.
         }
      }
       
             for (HostConfig hostConfig : RUBENS_HOST_CONFIGS) {
                StringBuilder url = new StringBuilder();
         url.append("http://").append(hostConfig.getHost()).append(":").append(hostConfig.getPort()).append("/");
         final Builder builder = REQUEST_MAKER.registerCallBack(HttpMethod.GET, url.toString());
         builder.url().headers().addHeader("host", hostConfig.getHost());
         if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("Requesting url %s", url.toString()));
         }
         try {
            REQUEST_MAKER.requestSync(builder.build(), hostConfig);
         }
         catch (final InvalidResponseException e) {
            LOGGER.error(String.format("Error in getting response %s", e));
         }
             }

       
    }

}
