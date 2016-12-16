package com.myland.pool;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myland.common.ReturnResult;
import com.myland.intfc.MsgConsumerService;
import com.myland.intfc.MsgSenderService;
import com.myland.util.MyBeanUtil;



/**
 * 多线程消费者池
 * @ClassName: MultiThreadConsumerPool 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月15日 下午7:02:47 
 *
 */
public class MultiThreadConsumerPool {
	
	public MultiThreadConsumerPool(ThreadPoolConsumerBuilder threadPoolConsumerBuilder) {
		this.infoHolder = threadPoolConsumerBuilder;
        executor = Executors.newFixedThreadPool(threadPoolConsumerBuilder.threadCount);
	}

	private static final Logger log = LoggerFactory.getLogger(MultiThreadConsumerPool.class);
	
	private ExecutorService executor;
	//线程在每次使用变量的时候，都会读取变量修改后的最的值
    private volatile boolean stop = false;
    private final ThreadPoolConsumerBuilder infoHolder;
    
    public static class ThreadPoolConsumerBuilder{
    	//线程数
    	int threadCount;
        long intervalMils;

        
        public ThreadPoolConsumerBuilder setThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public ThreadPoolConsumerBuilder setIntervalMils(long intervalMils) {
            this.intervalMils = intervalMils;
            return this;
        }

        public MultiThreadConsumerPool build() {
            return new MultiThreadConsumerPool(this);
        }
    }
    
    public void stop() {
        this.stop = true;
    }
    
    public void start() throws IOException {
    	for (int i = 0; i < infoHolder.threadCount; i++) {
    		MsgConsumerService<String> bean = (MsgConsumerService<String>) MyBeanUtil.getBean("msgConsumerService");
			final MsgConsumerService<String> consumer = bean; 
    		executor.execute(new Runnable() {
				@Override
				public void run() {
					while (!stop) {
						try{
							ReturnResult res = consumer.consume();
							if (infoHolder.intervalMils > 0) {
                                try {
                                    Thread.sleep(infoHolder.intervalMils);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    log.error("thread pool consumer interrupt: " + e);
                                }
                            }

                            if (!res.isSuc()) {
                                log.info("thread pool consumer run error: " + res.getErrMsg());
                            }
						}catch(Exception e){
							log.error("thread pool consumer exception: " + e);
						}
					}					
				}    			
    		});
    	}
    }

}
