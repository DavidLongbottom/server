package com.net.server;


import java.io.IOException;
import java.util.Properties;


import com.net.server.handler.CmdInfoHandler;
import com.net.server.handler.DescryptInboundHandller;
import com.net.server.handler.EncryptOutboundHandller;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;



public class Socks {
	NioEventLoopGroup bossGroup=new NioEventLoopGroup(1);
	NioEventLoopGroup workerGroup=new NioEventLoopGroup();
	
	static String base64KeyString;
    
    private void init(int port) 
    {	
    	ServerBootstrap serverBootstrap=new ServerBootstrap();
	    try {
	    	serverBootstrap.group(bossGroup, workerGroup)
	    					.channel(NioServerSocketChannel.class)
	    					.option(ChannelOption.SO_BACKLOG,1024)
	    					.childHandler(new ChannelInitializer<NioSocketChannel>() {
	
								@Override
								protected void initChannel(NioSocketChannel ch) throws Exception {
									ch.pipeline()
									// outbound 加密，并且长度
									  .addLast(new EncryptOutboundHandller(base64KeyString))
									//inbound
									  .addLast(new LengthFieldBasedFrameDecoder(10485760,0,4,0,4))
									  .addLast(new DescryptInboundHandller(base64KeyString))
									  .addLast(new CmdInfoHandler());  // 用完一次就丢
										  
								}
	    					    
							});
    		ChannelFuture future=serverBootstrap.bind(port).sync();
        	future.channel().closeFuture().sync();
		}catch (Exception e) {
			e.printStackTrace();
		} 
    	finally {
    		bossGroup.shutdownGracefully();
    		workerGroup.shutdownGracefully();
    	}
    }
    
    public static void main( String[] args ) throws IOException
    {
    	Socks socks=new Socks();
        
        Properties props=new Properties();
        props.load(Socks.class.getResourceAsStream("config.properties"));
        
        int port=Integer.parseInt(props.getProperty("port").trim());
        base64KeyString=props.getProperty("key").trim();
        		
    	socks.init(port);
 	   	
    }
    
    public NioEventLoopGroup getWorkerGroup() {
    	return workerGroup;
    }
    
    
}
