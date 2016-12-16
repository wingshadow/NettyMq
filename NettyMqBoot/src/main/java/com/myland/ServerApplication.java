package com.myland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.myland.util.MyBeanUtil;
/**
 * 主程序类
 * @ClassName: ServerApplication 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 上午11:41:02 
 *
 */
@SpringBootApplication(scanBasePackages = {"com.myland"})
public class ServerApplication {
	public static void main(String[] args){
//        SpringApplication app = new SpringApplication(ServerApplication.class);
//        app.setWebEnvironment(false);
//        app.run(args);
		
		final ApplicationContext applicationContext = 
                SpringApplication.run(ServerApplication.class, args);
		MyBeanUtil.setApplicationContext(applicationContext);
    }
}
