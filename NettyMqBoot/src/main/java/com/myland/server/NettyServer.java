package com.myland.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 
 * @ClassName: NettyServer 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 下午5:03:02 
 *
 */
@Component
public class NettyServer {
	private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
	
	@Value("${rpcServer.host:127.0.0.1}")
    String host;
	
	@Value("${rpcServer.ioThreadNum:5}")
    int ioThreadNum;
    //内核为此套接口排队的最大连接个数，对于给定的监听套接口，内核要维护两个队列，未链接队列和已连接队列大小总和最大值

    @Value("${rpcServer.backlog:1024}")
    int backlog;

    @Value("${rpcServer.port:18866}")
    int port;
	
	
	private ConnectionFactory connectionFactory;	
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel serverChannel;
	
	public NettyServer() {
		this.bossGroup = new NioEventLoopGroup();
		this.workerGroup = new NioEventLoopGroup();
	}	
	
	private void start(){
		try{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new MyChannelInitializer());
			
			//初始化服务端可连接队列数
			b.option(ChannelOption.SO_BACKLOG, backlog);
			//允许长连接
			b.childOption(ChannelOption.SO_KEEPALIVE, true);
			//通过NoDelay禁用Nagle,允许小数据即时传输
			b.childOption(ChannelOption.TCP_NODELAY, true);
			//允许重复使用本地地址和端口
			b.childOption(ChannelOption.SO_REUSEADDR, true);
			
		
			ChannelFuture f = b.bind(port).sync();
			serverChannel = f.channel();
			
			log.info("NettyServer start listening on port" + port +" and ready for connections...");
			
		}catch(Exception e){
			log.error("server start :"+e.getMessage());
		}
	}
	
	private void stop() {
		if (serverChannel != null) {
			serverChannel.close();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		
		serverChannel = null;
		workerGroup = null;
		bossGroup = null;

		log.info("Server is shut down");
	}
	
	@PreDestroy
	public void doStop(){
		stop();
	}
	
	@PostConstruct
	public void doStart(){
		start();
	}
}
