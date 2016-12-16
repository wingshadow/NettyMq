package com.myland.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

public class ChannelCache {
	
	private volatile static ChannelCache mapCache;// 缓存实例对象

	private static Map<String,SocketChannel> map=new ConcurrentHashMap<String, SocketChannel>();
	
	public static ChannelCache getInstance() {
		if (null == mapCache) {  
            synchronized (ChannelCache.class) {  
                if (null == mapCache) {  
                	mapCache = new ChannelCache();  
                }  
            }  
        }  
        return mapCache;
	}
	
    public static void add(String clientId,SocketChannel socketChannel){
    	if(map.get(clientId)==null){
    		map.put(clientId,socketChannel);
    	}        
    }
    public static Channel get(String clientId){
       return map.get(clientId);
    }
    public static void remove(SocketChannel socketChannel){
        for (Map.Entry entry:map.entrySet()){
            if (entry.getValue()==socketChannel){
                map.remove(entry.getKey());
            }
        }
    }

}
