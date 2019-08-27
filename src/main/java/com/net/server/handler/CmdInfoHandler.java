package com.net.server.handler;

import java.util.concurrent.CountDownLatch;

import org.msgpack.MessagePack;

import com.net.server.pojo.CmdInfo;
import com.net.server.pojo.CmdResult;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;

public class CmdInfoHandler extends SimpleChannelInboundHandler<Object> {

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, Object msg) throws Exception {
		
		final CountDownLatch startSignal=new CountDownLatch(1);
		
		if(msg instanceof ByteBuf) {
			
			ByteBuf msgBuf=(ByteBuf)msg;
			byte[] msgByte=ByteBufUtil.getBytes(msgBuf);
			
			MessagePack messagePack=new MessagePack();
			CmdInfo cmdInfo=messagePack.read(msgByte,CmdInfo.class);

			Bootstrap bootstrap=new Bootstrap();
			bootstrap.group(ctx.channel().eventLoop())
					 .channel(NioSocketChannel.class)
					 .handler(new ChannelInitializer<NioSocketChannel>() {

						@Override
						protected void initChannel(NioSocketChannel ch) throws Exception {
							ch.pipeline().addLast(new TransInboundHandler((NioSocketChannel)ctx.channel()));
							
							startSignal.countDown();

						}
					 });
			
			ChannelFuture future=bootstrap.connect(cmdInfo.getHost(),cmdInfo.getPort());
			future.addListener(new ChannelFutureListener() {

				public void operationComplete(ChannelFuture future) throws Exception {

					CmdResult cmdResult=new CmdResult();
					if(future.isSuccess()) {
						cmdResult.setConnectStatus(true);
					}else {
						cmdResult.setConnectStatus(false);

					}
					MessagePack msp=new MessagePack();
					byte[] cmdResultByte=msp.write(cmdResult);
					
					
					ctx.pipeline().remove(CmdInfoHandler.this);
					ctx.pipeline().addLast(new TransInboundHandler((NioSocketChannel)future.channel()));
		
					startSignal.await();
					ctx.writeAndFlush(Unpooled.copiedBuffer(cmdResultByte));
				}
				
			});
			
			
		}
		
	}
	
	
	
	

}
