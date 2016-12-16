package com.myland.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.myland.common.Constants;
import com.myland.common.RetryCache;
import com.myland.common.ReturnResult;
import com.myland.intfc.MsgSenderService;
import com.myland.util.MyBeanUtil;

/**
 * 发送类
 * @ClassName: MsgSenderServiceImpl 
 * @Description: TODO 
 * @author zhb 
 * @date 2016年12月14日 下午8:30:03 
 *
 */
@Service("msgSenderService")
public class MsgSenderServiceImpl implements MsgSenderService<String> {

	private static final Logger log = LoggerFactory.getLogger(MsgSenderServiceImpl.class);
	
	@Resource
	private ConnectionFactory connectionFactory;
	
	private RabbitTemplate rabbitTemplate;
	
	@Value("${sender.exchange}")
	private String exchange ;
	@Value("${sender.routingKey}")
	private String routingKey;
	@Value("${sender.queue}")
	private String queue;
	
	public MsgSenderServiceImpl() {
		
	}

	@Override
	public ReturnResult send(String message) {
		//发送消息队列，必须初始化对象，否则不能执行回调函数
		connectionFactory = (ConnectionFactory)MyBeanUtil.getBean("connectionFactory");
		this.rabbitTemplate = new RabbitTemplate(connectionFactory); 
		
		rabbitTemplate.setMandatory(true);
        rabbitTemplate.setExchange(exchange);	
        rabbitTemplate.setRoutingKey(routingKey);
        
        RetryCache retryCache = new RetryCache();
        //确认消息是否到达broker服务器，也就是只确认是否正确到达exchange中即可，只要正确的到达exchange中，broker即可确认该消息返回给客户端ack。
        rabbitTemplate.setConfirmCallback(new ConfirmCallback(){
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				if (!ack) {
	                log.info("send message failed: " + cause + correlationData.toString());
	            } else {
	                retryCache.del(correlationData.getId());
	                log.info("send message suc: " );
	            }
				
			}
        });
        
        //确认消息是否到达broker服务器，也就是只确认是否正确到达exchange中即可，只要正确的到达exchange中，broker即可确认该消息返回给客户端ack。
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
			@Override
			public void returnedMessage(Message message, int replyCode, String replyText, String exchange,
					String routingKey) {
				try {
	                Thread.sleep(Constants.ONE_SECOND);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	            rabbitTemplate.send(message);				
			}
        });        
        retryCache.setSender(this);
        try {
            String id = retryCache.generateId();
            retryCache.add(id, message);
            rabbitTemplate.correlationConvertAndSend(message, new CorrelationData(id));
        } catch (Exception e) {
        	e.printStackTrace();
            return new ReturnResult(false,"1001", e.getMessage());
        }
		return new ReturnResult(true,"0000","");
	}

}
