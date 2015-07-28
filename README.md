# Netty HttpClient

A high performance netty based http client.

This is a pure http client using netty encoder/decoder pattern and is a high performance client due to non-blocking I/O.


# Supported Types of HTTP connections and patterns
 
## Connection Types
The connections made to a host can be of different types due to performance reasons. For high performance always go with persistent connections. But the caveat is that this can be used only in a server to server back channel and not for interactive queries from clients.


### OPEN_CLOSE
One connection per http request. The connection is opened just before a request and closed after a request is complete or timed out.

### PERSISTENT

The connection is persistent meaning the connections per host can be configured and they are maintained internally by the library in a pool. Closed connections will be automatically replaced with new connections. For constantly changing ip addresses always enable lookup of name in java options.

## Waiting for response

The response of an http operation is by default blocking but with netty the response is actually non blocking. The blocking model implemented here uses memory and not to be considered for production use . Always prefer asynchronous operation but the synchronous one will be useful if you have lots of memory. The advantage of using synchronous operation on top of asynchronous is to have a thread blocking semantics but releasing the burden of the end server which handles the request.



### Sync
Sync means synchronous way of calling an http request. The http request will wait until the response is got.

### Async
This will be useful when you need an asynchrounous request to happen and get called back for the response in future.



# How To
## Step 1: Obtain a static Request Maker singleton
Store the singleton is a static reference where ever you want to use it.
<pre>
private static final RequestMaker REQUEST_MAKER = RequestMaker.getInstance();
</pre>
Also create a logger object for logging.
<pre>
private static final Logger LOGGER = Logger.getLogger(ClassName.class);
</pre>
Note: Replace class name with the name of your class
## Step 2: Create and register a host. 
This needs to be a one time operation per host and best done in a config class.
<pre>
static {
HostConfig hostConfig = null;
try {
hostConfig = new HostConfig("google.com", 80, SyncType.OPENCLOSE);
REQUEST_MAKER.registerHost(hostConfig);
} catch (final AlreadyRegisteredHostException e) {
LOGGER.error("Already registered");
}
}
</pre>
## Step 3: Create endpoint
This will be the endpoint to call. The default headers can be added using headers() method.
<pre>
final Builder endPoint = REQUEST_MAKER.registerCallBack(HttpMethod.GET,
"http://google.com?q=Sync");
endPoint.url().headers();
</pre>
## Step 4: Fire the Http query
<pre>
REQUEST_MAKER.requestASync(endPoint.build(), hostConfig,
                  new ExternalCallback() {
                            @Override
                            public void httpResponse(final ResponseMsg response) {
                                        if (LOGGER.isTraceEnabled()) {
                                            LOGGER.trace(response.getResponse());
                                        }

                                    }
                  });
</pre>
Note: In case of Synchronous call use requestSync and use the result.getResponse() to get the response.



Use the sample classes in the project to understand how different patterns for calling a http request works.

## Examples

Examples are available in the main package - SyncHttpClient, AsyncHttpClient

Both follow the same pattern of registering a host first(Need to be done once in a static block),building an endpoint.
The only difference between sync and async is that in case of sync the call blocks and in case of async the callback is called in future but call returns.


### Sync+OPEN_CLOSE Example

Refer: https://github.com/arungeorge81/netty-http-client/blob/master/src/main/java/http/io/asynchttpclient/SyncHttpClient.java
 
### Async+PERSISTENT

Refer: https://github.com/arungeorge81/netty-http-client/blob/master/src/main/java/http/io/asynchttpclient/AsyncHttpClient.java

