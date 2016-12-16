package com.myland.util;

import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 接收消息处理类
 * @ClassName: MsgUtils 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 下午2:05:15 
 *
 */
public class MsgDecodeUtils extends ByteToMessageDecoder{
	
	private static int HEAD_LENGTH = 16;

	/**
	 * 数据包解码
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//标记读取当前位置
		in.markReaderIndex();
		//获取msg长度
		int msgLength = in.readInt();
		if (in.readableBytes() < msgLength) {
			in.resetReaderIndex();
			return;
		}
		
		ByteBuf bf = in.readBytes(msgLength);
		byte[] data = bf.array();	
		
		Arrays.copyOfRange(data, 0, 16);
		

	}
	
	public static void main(String arg[]){
		byte[] a = {1,2,3,4,5};
		byte[] b = Arrays.copyOfRange(a, 0, 3);
		for(int value:b){
			
			System.out.println(value);
		}
	}
}
