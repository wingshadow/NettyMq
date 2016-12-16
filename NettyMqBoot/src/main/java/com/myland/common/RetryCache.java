package com.myland.common;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.myland.intfc.MsgSenderService;


/***
 * 如果网络中断重发消息
 * @ClassName: RetryCache 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 下午8:12:46 
 *
 */
public class RetryCache {
    private MsgSenderService sender;
    private boolean stop = false;
    private Map<String, MessageWithTime> map = new ConcurrentHashMap<String, MessageWithTime>();
    private AtomicLong id = new AtomicLong();


    private static class MessageWithTime {
        public MessageWithTime(long currentTimeMillis, Object message2) {
			// TODO Auto-generated constructor stub
		}
		long time;
        Object message;
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
		public Object getMessage() {
			return message;
		}
		public void setMessage(Object message) {
			this.message = message;
		}
        
    }

    public void setSender(MsgSenderService sender) {
        this.sender = sender;
        startRetry();
    }

    public String generateId() {
        return "" + id.incrementAndGet();
    }

    public void add(String id, Object message) {
        map.put(id, new MessageWithTime(System.currentTimeMillis(), message));
    }

    public void del(String id) {
        map.remove(id);
    }

    private void startRetry() {
        new Thread(() ->{
            while (!stop) {
                try {
                    Thread.sleep(Constants.RETRY_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long now = System.currentTimeMillis();

                for (String key : map.keySet()) {
                    MessageWithTime messageWithTime = map.get(key);

                    if (null != messageWithTime) {
                        if (messageWithTime.getTime() + 3 * Constants.VALID_TIME < now) {
//                            log.info("send message failed after 3 min " + messageWithTime);
                            del(key);
                        } else if (messageWithTime.getTime() + Constants.VALID_TIME < now) {
                        	ReturnResult result = sender.send(messageWithTime.getMessage());

                            if (result.isSuc()) {
                                del(key);
                            }
                        }
                    }
                }
            }
        }).start();
    }
}
