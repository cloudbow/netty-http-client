


package pns.alltypes.netty.httpclient.response;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

import java.io.IOException;

import org.apache.log4j.Logger;

import pns.alltypes.netty.httpclient.codec.HttpRequestMessageEncoder;
import pns.alltypes.netty.httpclient.sync.RequestResponeSync;

/**
 * The Class HttpResponseResourceHandler.
 * @author arung
 */
public class HttpResponseResourceHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(HttpResponseResourceHandler.class);

    /** The Constant REQUEST_RESPONE_SYNC. */
    private static final RequestResponeSync REQUEST_RESPONE_SYNC = RequestResponeSync.getInstance();

    /** The throwable. */
    private Throwable throwable;

    /*
     * (non-Javadoc)
     * @see
     * io.netty.channel.SimpleChannelInboundHandler#messageReceived(io.netty
     * .channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpResponse msg) throws Exception {

        try {
            final String content = msg.content().toString(CharsetUtil.UTF_8);
            if (HttpResponseResourceHandler.LOGGER.isTraceEnabled()) {
                HttpResponseResourceHandler.LOGGER.trace(String.format("Received content %s", content));
            }

            HttpResponseResourceHandler.REQUEST_RESPONE_SYNC.putResponse(
                    ctx.pipeline().get(HttpRequestMessageEncoder.class).getHttpRequestMessage(), new ResponseMsg(
                            content, msg.status()));

        } finally {
            HttpResponseResourceHandler.REQUEST_RESPONE_SYNC.resume(ctx.pipeline().get(HttpRequestMessageEncoder.class)
                    .getHttpRequestMessage());
        }

    }

    /*
     * (non-Javadoc)
     * @see
     * io.netty.channel.ChannelHandlerAdapter#exceptionCaught(io.netty.channel
     * .ChannelHandlerContext, java.lang.Throwable)
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {

        if (cause instanceof IOException) {
            ctx.close();
        } else {
            throwable = cause;
            HttpResponseResourceHandler.REQUEST_RESPONE_SYNC.resume(ctx.pipeline().get(HttpRequestMessageEncoder.class)
                    .getHttpRequestMessage());
        }

    }

    /** Gets the throwable.
     * @return the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /** Sets the throwable.
     * @param throwable
     *            the new throwable
     */
    public void setThrowable(final Throwable throwable) {
        this.throwable = throwable;
    }

}
