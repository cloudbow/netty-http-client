
package pns.alltypes.netty.httpclient.codec;

import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import pns.alltypes.netty.httpclient.request.HttpRequestMessage;

/**
 * The Class HttpRequestMessageEncoder.
 * @author arung
 */

public class HttpRequestMessageEncoder extends MessageToMessageEncoder<HttpRequestMessage> {

    /** The Constant logger. */
    private static final Logger LOGGER = Logger.getLogger(HttpRequestMessageEncoder.class.getName());

    /** The http request message. */
    private HttpRequestMessage httpRequestMessage;

    /*
     * (non-Javadoc)
     * @see io.netty.handler.codec.MessageToByteEncoder#encode(io.netty.channel.
     * ChannelHandlerContext, java.lang.Object, io.netty.buffer.ByteBuf)
     */
    @Override
    protected void encode(final ChannelHandlerContext ctx, final HttpRequestMessage msg, final List<Object> out) throws Exception {
        if (HttpRequestMessageEncoder.LOGGER.isTraceEnabled()) {
            HttpRequestMessageEncoder.LOGGER.trace(String.format("Started encoding message %s", msg));
        }
        setHttpRequestMessage(msg);

        if (msg.getHttpMethod().equals(HttpMethod.GET)) {

            final FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, msg.getHttpMethod(), msg.getUrl());

            addProvidedHeaders(msg, request);
            out.add(request);

            if (HttpRequestMessageEncoder.LOGGER.isTraceEnabled()) {
                HttpRequestMessageEncoder.LOGGER.trace(String.format("Encoded message for %s is %s", msg, request));
            }
        } else if (msg.getHttpMethod().equals(HttpMethod.PUT) || msg.getHttpMethod().equals(HttpMethod.POST)) {
            final HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, msg.getHttpMethod(), msg.getUrl());
            byte[] contentBytes = new byte[0];
            contentBytes = msg.getBody().getBytes(CharsetUtil.UTF_8);
            final ByteBuf buf = ctx.alloc().directBuffer();
            buf.writeBytes(contentBytes);
            request.headers().add(msg.getHeaders());
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentBytes.length);
            final HttpContent content = new DefaultLastHttpContent(buf);
            out.add(request);
            out.add(content);

            if (HttpRequestMessageEncoder.LOGGER.isTraceEnabled()) {
                HttpRequestMessageEncoder.LOGGER.trace(String.format("Encoded message for %s is %s, %s", msg, request, content));
            }
        }

        ctx.flush();
    }

    private void addProvidedHeaders(final HttpRequestMessage msg, final FullHttpRequest request) {
        request.headers().add(msg.getHeaders());
    }

    /**
     * Gets the http request message.
     * @return the http request message
     */
    public HttpRequestMessage getHttpRequestMessage() {
        return httpRequestMessage;
    }

    /**
     * Sets the http request message.
     * @param httpRequestMessage
     *            the new http request message
     */
    public void setHttpRequestMessage(final HttpRequestMessage httpRequestMessage) {
        this.httpRequestMessage = httpRequestMessage;
    }
}
