/**
 *
 */
package pns.alltypes.netty.httpclient;

import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.config.HostConfig;
import pns.alltypes.netty.httpclient.exception.AlreadyRegisteredHostException;
import pns.alltypes.netty.httpclient.exception.InvalidResponseException;
import pns.alltypes.netty.httpclient.request.HttpRequestMessage.Builder;
import pns.alltypes.netty.httpclient.request.RequestMaker;
import pns.alltypes.netty.httpclient.response.ExternalCallback;
import pns.alltypes.netty.httpclient.response.ResponseMsg;

// TODO: Auto-generated Javadoc
/**
 * The Class AsyncHttpClient.
 * @author arung
 */
public class AsyncHttpClient {

    /** The Constant URL. */
    private static final String URL = "http://google.com?q=async";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(AsyncHttpClient.class);

    /** The Constant ASYNC_HTTP_CLIENT. */
    private static final AsyncHttpClient ASYNC_HTTP_CLIENT = new AsyncHttpClient();

    /** The Constant REQUEST_MAKER. */
    private static final RequestMaker REQUEST_MAKER = RequestMaker.getInstance();

    /**
     * The main method.
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        LogManager.getRootLogger().setLevel(Level.TRACE);
        AsyncHttpClient.ASYNC_HTTP_CLIENT.sendRequest();

    }

    /**
     * Send request.
     */
    private void sendRequest() {
        HostConfig hostConfig = null;
        try {
            hostConfig = new HostConfig("google.com");
            AsyncHttpClient.REQUEST_MAKER.registerHost(hostConfig);
        } catch (final AlreadyRegisteredHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final HostConfig hostConfig2 = hostConfig;
        final ExecutorService service = Executors.newFixedThreadPool(3);
        ((ThreadPoolExecutor) service).prestartAllCoreThreads();
        final List<Callable<Boolean>> taskList = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < 1; i++) {
            taskList.add(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {

                    final Builder registerCallBack = AsyncHttpClient.REQUEST_MAKER.registerCallBack(HttpMethod.GET, AsyncHttpClient.URL);
                    registerCallBack.url().headers();
                    try {
                        AsyncHttpClient.REQUEST_MAKER.requestASync(registerCallBack.build(), hostConfig2, new ExternalCallback() {

                            @Override
                            public void httpResponse(final ResponseMsg response) {
                                if (AsyncHttpClient.LOGGER.isTraceEnabled()) {
                                    AsyncHttpClient.LOGGER.trace(response.getResponse());
                                }

                            }
                        });
                    } catch (final InvalidResponseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;
                }

            });

        }
        List<Future<Boolean>> invokeAll = null;

        try {
            invokeAll = service.invokeAll(taskList);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (final Future<Boolean> future : invokeAll) {
            try {
                future.get();
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
