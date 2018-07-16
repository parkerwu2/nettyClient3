package com.example.demo.imclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by jingzhi.wu on 2018/7/13.
 */
@Slf4j
public class ImConnection {
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private Channel channel;

    public Channel connect(String host, int port) {
        doConnect(host, port);
        return this.channel;
    }

    private void doConnect(String host, int port) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.handler(new LoggingHandler(LogLevel.INFO));
//            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {

                    ChannelPipeline pipe = ch.pipeline();
                    // Add the text line codec combination first,
                    pipe.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    // the encoder and decoder are static as these are sharable
                    pipe.addLast(DECODER);
                    pipe.addLast(ENCODER);
                    ch.pipeline().addLast(new TimeClientHandler());

                }
            });

            ChannelFuture f = b.connect(host, port);
            log.info("重连结束...");
//            f.addListener(new ConnectionListener());
//            channel = f.channel();
            NettyClient.doSendAndReceive(f);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
