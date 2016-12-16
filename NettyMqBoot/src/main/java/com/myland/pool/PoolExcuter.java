package com.myland.pool;

import java.io.IOException;

import com.myland.common.Constants;

public class PoolExcuter {

	public PoolExcuter() {
		// TODO Auto-generated constructor stub
	}

	private MultiThreadConsumerPool pool;
	
	public void init(){
		pool = new MultiThreadConsumerPool.ThreadPoolConsumerBuilder()
				.setThreadCount(Constants.THREAD_COUNT)
				.setIntervalMils(Constants.INTERVAL_MILS).build();
	}
	
	public void start() throws IOException {
		pool.start();
    }

    public void stop() {
    	pool.stop();
    }
}
