package com.example.demo.imclient;


import com.example.demo.bean.IMMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageClientHandler extends SimpleChannelInboundHandler<IMMessage> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
		System.out.println("get the msg >>> "+msg.getBody());
	}

}
