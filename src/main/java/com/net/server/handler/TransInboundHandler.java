package com.net.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;


// 注意这个命名Inbound 是因为pipelineA 来说是数据的流入， 而写入到另一个pipelineB，但对B 是Outbound
public class TransInboundHandler extends SimpleChannelInboundHandler<Object> {

	private NioSocketChannel destChannel;
	
	public TransInboundHandler(NioSocketChannel destChannel) {
		this.destChannel=destChannel;
	}
	
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			ByteBuf msgBuf=(ByteBuf)msg;
			destChannel.writeAndFlush(msgBuf);
		}else {
			System.out.println("server Trans 不是ByteBuf ");
			destChannel.writeAndFlush(msg);

		}

	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	
	}
	
	

}
