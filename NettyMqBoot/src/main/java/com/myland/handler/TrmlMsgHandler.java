package com.myland.handler;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.myland.common.ChannelCache;
import com.myland.intfc.MsgSenderService;
import com.myland.util.MyBeanUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TrmlMsgHandler extends ChannelInboundHandlerAdapter{

	private static final Logger log = LoggerFactory.getLogger(TrmlMsgHandler.class);
	
	private MsgSenderService<String> msgSenderService;	
	//管理所有channel通道
	public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
		
		ByteBuf in = (ByteBuf) msg;
		String receiveMsg = in.toString(io.netty.util.CharsetUtil.UTF_8);
		log.info("Server Receive Client Msg:"+receiveMsg);
		
		/**
		 * 解析Msg,客户端ID作为主键,channel写入缓存
		 * ChannelCache.getInstance().add("Test1", (SocketChannel)ctx.channel());
		 */
		
		msgSenderService =(MsgSenderService) MyBeanUtil.getBean("msgSenderService");
		msgSenderService.send(receiveMsg);
		
		String backMsg = "send client:"+System.currentTimeMillis();
		ChannelCache.getInstance().get("Test1").writeAndFlush(Unpooled.copiedBuffer(backMsg.getBytes("utf-8")));
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//关闭channel会被channelGroup自动移除
		channels.add(ctx.channel());	

		//为了方便测试加入以下代码，正式使用时需要去掉
		ChannelCache.getInstance().add("Test1", (SocketChannel)ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//channel关闭、断开、未绑定执行该方法，从缓存中移除channel
		ChannelCache.getInstance().remove((SocketChannel)ctx.channel());
	}
	
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		 ctx.flush();  
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.close();
		log.warn(cause.getMessage());
	}

}
