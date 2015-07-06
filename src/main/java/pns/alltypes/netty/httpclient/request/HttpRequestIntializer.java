


package pns.alltypes.netty.httpclient.request;

import pns.alltypes.netty.httpclient.codec.HttpRequestMessageEncoder;
import pns.alltypes.netty.httpclient.response.HttpResponseResourceHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

/**
 * The Class HttpRequestIntializer.
 * @author arung
 */

public class HttpRequestIntializer extends ChannelInitializer<SocketChannel> {

    /*
     * (non-Javadoc)
     * @see
     * io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
     */
    @Override
    protected void initChannel(final SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();

        // pipeline.addLast("codec", new HttpClientCodec());

        pipeline.addLast("encoder1", new HttpRequestEncoder());

        pipeline.addLast("encoder", new HttpRequestMessageEncoder());

        pipeline.addLast("decoder1", new HttpResponseDecoder());
        pipeline.addLast("inflator", new HttpContentDecompressor());
        // // Remove the following line if you don't want automatic content
        // // decompression.

        pipeline.addLast("aggregator", new HttpObjectAggregator(512 * 1024));

        // // to be used since huge file transfer
        // pipeline.addLast(BatchMessageInitializer.CHUNKED_WRITER,
        // new ChunkedWriteHandler());

        // // On top of the SSL handler, add the text line codec.
        // pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,
        // Delimiters.lineDelimiter()));
        // pipeline.addLast("decoder",
        // applicationContext.getBean(BatchConnectionDecoder.class));
        // pipeline.addLast("encoder",
        // applicationContext.getBean(BatchConnectionEncoder.class));
        // pipeline.addLast(BatchMessageInitializer.CODEC,
        // applicationContext.getBean(BatchMessageCodec.class));
        // and then business logic.
        pipeline.addLast("handler", new HttpResponseResourceHandler());
    }

}
