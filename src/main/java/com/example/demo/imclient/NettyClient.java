package com.example.demo.imclient;

import com.example.demo.bean.IMMessage;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class NettyClient {

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();
	private static EventLoopGroup workerGroup;
	public static void main(String[] args) {
		String host = "127.0.0.1";
        int port = 1000;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap b = new Bootstrap(); // (1)
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
            
            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            IMMessage msg = new IMMessage();
            msg.setBody("ccc");
            msg.setFrom("aaa");
            msg.setTo("to");
            msg.setType("1");
           
           // f.channel().writeAndFlush(msg);
            Channel ch = f.channel();
            //f.channel().writeAndFlush("hi ....");
         // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }

            // Wait until the connection is closed.
            //f.channel().closeFuture().sync();
        } catch(Exception ex){
        	ex.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
	}
    public static void shutDown(){
        workerGroup.shutdownGracefully();
    }

    public static ChannelFuture connect(String host, int port){
        workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
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

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)
            return f;

            // Wait until the connection is closed.
            //f.channel().closeFuture().sync();
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static void doSendAndReceive(ChannelFuture f) throws IOException, InterruptedException {
        IMMessage msg = new IMMessage();
        msg.setBody("ccc");
        msg.setFrom("aaa");
        msg.setTo("to");
        msg.setType("1");

        // f.channel().writeAndFlush(msg);
        Channel ch = f.channel();
        //f.channel().writeAndFlush("hi ....");
        // Read commands from the stdin.
        ChannelFuture lastWriteFuture = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("开始交互");
        for (; ; ) {
            try {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            } catch (Exception e) {
                System.out.print("远程主机尚未连接，重连等待3秒");
                e.printStackTrace();
                Thread.currentThread().sleep(3000);
            }
        }

        // Wait until all messages are flushed before closing the channel.
        if (lastWriteFuture != null) {
            lastWriteFuture.sync();
        }
        System.out.println("交互结束");
    }
}
