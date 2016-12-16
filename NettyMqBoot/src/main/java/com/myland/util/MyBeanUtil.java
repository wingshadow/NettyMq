package com.myland.util;

import org.springframework.context.ApplicationContext;

public class MyBeanUtil {
	
	private static ApplicationContext applicationContext;
	
	public MyBeanUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public static void setApplicationContext(ApplicationContext context) {
	    applicationContext = context;
	  }

	public static Object getBean(String name){
		return applicationContext.getBean(name);
	}
}
