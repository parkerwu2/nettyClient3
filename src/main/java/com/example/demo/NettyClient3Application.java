package com.example.demo;

import com.example.demo.bean.IMMessage;
import com.example.demo.consts.Const;
import com.example.demo.imclient.NettyClient;
import com.example.demo.imclient.TimeClientHandler;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class NettyClient3Application {
	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();
	public static boolean reconnect = true;

//	public void connect(int port, String host) throws Exception {
//		// Configure the client.
//		EventLoopGroup workerGroup = new NioEventLoopGroup();
//		ChannelFuture future = null;
//		try {
//			Bootstrap b = new Bootstrap(); // (1)
//			b.group(workerGroup); // (2)
//			b.channel(NioSocketChannel.class); // (3)
//			b.handler(new LoggingHandler(LogLevel.INFO));
////            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
//			b.handler(new ChannelInitializer<SocketChannel>() {
//				@Override
//				public void initChannel(SocketChannel ch) throws Exception {
//					ChannelPipeline pipe = ch.pipeline();
//					// Add the text line codec combination first,
//					pipe.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//					// the encoder and decoder are static as these are sharable
//					pipe.addLast(DECODER);
//					pipe.addLast(ENCODER);
//					ch.pipeline().addLast(new TimeClientHandler());
//				}
//			});
//
//			future = b.connect(host, port).sync();
//			future.channel().closeFuture().sync();
//		} finally {
////          group.shutdownGracefully();
//			if (null != future) {
//				if (future.channel() != null && future.channel().isOpen()) {
//					future.channel().close();
//				}
//			}
//			System.out.println("准备重连");
//			connect(port, host);
//			System.out.println("重连成功");
//		}
//	}

	private static ChannelFuture connect(String host, int port){
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
			return f;

			// Wait until the connection is closed.
			//f.channel().closeFuture().sync();
		} catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	private static void doSendAndReceive(ChannelFuture f) throws IOException, InterruptedException {
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
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		SpringApplication.run(NettyClient3Application.class, args);
		try {
			ChannelFuture f = NettyClient.connect(Const.SERVER_URL, Const.SERVER_PORT);
			NettyClient.doSendAndReceive(f);
		} catch(Exception ex){
			ex.printStackTrace();
		}finally {
			NettyClient.shutDown();
		}

//		String host = Const.SERVER_URL;
//		int port = Const.SERVER_PORT;
//		EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//		try {
//			// Start the client.
//			ChannelFuture f = connect(host, port); // (5)
//			if (f != null) {
//				doSendAndReceive(f);
//			}
//
//			// Wait until the connection is closed.
//			//f.channel().closeFuture().sync();
//		} catch(Exception ex){
//			ex.printStackTrace();
//			Thread.sleep(3 * 1000);
//			System.out.println("begin to reconnect");
//			ChannelFuture f = connect(host, port); // (5)
//			if (f != null) {
//				doSendAndReceive(f);
//			}
//		} finally {
//			if (reconnect){
//				Thread.sleep(3 * 1000);
//				System.out.println("begin to reconnect");
//				ChannelFuture f = connect(host, port); // (5)
//				if (f != null) {
//					doSendAndReceive(f);
//				}
//			} else {
//				workerGroup.shutdownGracefully();
//			}
//		}
	}
}
