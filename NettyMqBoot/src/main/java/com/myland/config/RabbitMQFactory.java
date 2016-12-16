package com.myland.config;

import org.springframework.amqp.rabbit.connection.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

/**
 * 创建RabbitMq连接器
 * @ClassName: RabbitMQFactory 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 下午7:13:15 
 *
 */
@Configuration
public class RabbitMQFactory{
	
	@Value("${rabbitmq.ip:127.0.0.1}")
    String host;
    @Value("${rabbitmq.port:5657}")
    int port;
    @Value("${rabbitmq.username:guest}")
    String userName;
    @Value("${rabbitmq.password:guest}")
    String password;
    
    
    @Bean
    public ConnectionFactory connectionFactory() {
//        Properties properties = new Properties();
//
//        try {
//            Resource res = new ClassPathResource("rabbitmq.properties");
//            properties.load(res.getInputStream());
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to load rabbitmq.properties!");
//        }

//        String ip = properties.getProperty("ip");
//        int port = Integer.valueOf(properties.getProperty("port"));
//        String userName = properties.getProperty("user_name");
//        String password = properties.getProperty("password");

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);

        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true); // enable confirm mode
        connectionFactory.setPublisherReturns(true);

        return connectionFactory;
    }
}
