package com.myland.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


/**
 * 
 * @ClassName: HeartBeatHandler 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 上午11:47:50 
 *
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

	private static final Logger log = LoggerFactory
			.getLogger(HeartBeatHandler.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				log.info("心跳超时: read timeout from "+ ctx.channel().remoteAddress());
			}
		}
	}
}
