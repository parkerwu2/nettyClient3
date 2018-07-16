package com.example.demo.imclient;

import com.example.demo.consts.Const;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TimeClientHandler extends SimpleChannelInboundHandler<String> {
	private ImConnection imConnection = new ImConnection();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(" get the string msg >>> "+msg);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.err.println("掉线了...");
		//使用过程中断线重连
		final EventLoop eventLoop = ctx.channel().eventLoop();
		eventLoop.schedule(new Runnable() {
			@Override
			public void run() {
				log.info("尝试重连...");
				imConnection.connect(Const.SERVER_URL, Const.SERVER_PORT);
			}
		}, 20L, TimeUnit.SECONDS);

		super.channelInactive(ctx);
	}
}
