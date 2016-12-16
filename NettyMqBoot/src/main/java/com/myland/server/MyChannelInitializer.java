package com.myland.server;


import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.myland.handler.HeartBeatHandler;
import com.myland.handler.TrmlMsgHandler;
import com.myland.intfc.MsgSenderService;
import com.myland.util.MsgDecodeUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 初始化Channel添加处理handler
 * @ClassName: MyChannelInitializer 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 下午1:58:43 
 *
 */
public class MyChannelInitializer extends ChannelInitializer<SocketChannel>{
	
	//分隔符 0x7e
	private final static String DELIMITER_CHAR = "~";
	
	ByteBuf delimiter = Unpooled.copiedBuffer(DELIMITER_CHAR.getBytes(),DELIMITER_CHAR.getBytes());
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new IdleStateHandler(3 * 60, 0, 0));
		ch.pipeline().addLast(new HeartBeatHandler());
		//分隔数据包，格式：0x7e......0x7e
//		ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
//		ch.pipeline().addLast(new MsgDecodeUtils());
		ch.pipeline().addLast(new TrmlMsgHandler());
	}
}
