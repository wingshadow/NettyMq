package com.myland.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.myland.common.Constants;
import com.myland.common.FastJsonMessageConverter;
import com.myland.common.ReturnResult;
import com.myland.intfc.MsgConsumerService;
import com.myland.util.MyBeanUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

@Service("msgConsumerService")
public class MsgConsumerServiceImpl implements MsgConsumerService<String>{

	private static final Logger log = LoggerFactory.getLogger(MsgConsumerServiceImpl.class);
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	private RabbitTemplate rabbitTemplate;
	
	@Value("${receiver.exchange}")
	private String exchange ;
	@Value("${receiver.routingKey}")
	private String routingKey;
	@Value("${receiver.queue}")
	private String queue;
	@Value("${receiver.model}")
	private String model;
	
	public MsgConsumerServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ReturnResult consume() {

		Channel channel = null;
		QueueingConsumer consumer = null;
		
		connectionFactory = (ConnectionFactory)MyBeanUtil.getBean("connectionFactory");			
		final Connection connection = connectionFactory.createConnection();
		
		try{
			
			buildQueue(exchange,model,routingKey, queue, connection);
			//绑定路由器和队列
//			channel = connection.createChannel(false);
//			channel.exchangeDeclare(exchange, model, true, false, null);
//			channel.queueDeclare(queue, true, false, false, null);
//			channel.queueBind(queue, exchange, routingKey);
//			//通过 BasicQos 方法设置prefetchCount = 1。这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
//            //换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
//            channel.basicQos(1);            
//			
//			QueueingConsumer consumer = new QueueingConsumer(channel);
//			//channel.basicConsume接收消息时使autoAck为false，即不自动会发ack，由channel.basicAck()在消息处理完成后发送消息
//			channel.basicConsume(queue, false, consumer);
			
			consumer = buildQueueConsumer(connection, queue);
			channel = consumer.getChannel();
			
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			
			//自定义msg转换器
			final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
	        final MessageConverter messageConverter = new FastJsonMessageConverter();
			
			Message message = new Message(delivery.getBody(),
                    messagePropertiesConverter.toMessageProperties(delivery.getProperties(), 
                    		delivery.getEnvelope(), "UTF-8"));
			
			//JSON格式msg
			String msg = (String)messageConverter.fromMessage(message);
			
			
			//业务处理
			log.debug("receive msg from RabbitMQ:"+msg);
			
			ReturnResult res =new ReturnResult(true,"0000","");
			if(res.isSuc){
				//手动发送ack消息
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}else{
				//避免过多失败log
                Thread.sleep(Constants.ONE_SECOND);
                log.info("process message failed: " + res.getErrMsg());
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
			}			
			return res;
		}catch(InterruptedException e){
			return new ReturnResult(false, "1001",e.getLocalizedMessage());
		}catch (ShutdownSignalException | ConsumerCancelledException | IOException e){
			try {
                channel.close();
            } catch (Exception  ex) {
                ex.printStackTrace();
            }
			consumer = buildQueueConsumer(connection, queue);
			return new ReturnResult(false,"1001","shutdown or cancelled exception " + e.toString());
		}catch (Exception e){
			 try {
                 channel.close();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }

             consumer = buildQueueConsumer(connection, queue);
             return new ReturnResult(false,"1001","exception " + e.toString());
		}
	}
	/**
	 * 绑定路由器
	 * @Title: buildQueue 
	 * @Description: TODO
	 * @param @param exchange
	 * @param @param model
	 * @param @param routingKey
	 * @param @param queue
	 * @param @param connection
	 * @param @throws IOException
	 * @return void
	 * @throws
	 */
	private void buildQueue(String exchange, String model,String routingKey, final String queue, Connection connection)
			throws IOException {
		Channel channel = connection.createChannel(false);
		channel.exchangeDeclare(exchange, model, true, false, null);
		channel.queueDeclare(queue, true, false, false, null);
		channel.queueBind(queue, exchange, routingKey);

		try {
			channel.close();
		} catch (Exception e) {			
			log.error("close channel time out " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 绑定队列返回消费者
	 * @Title: buildQueueConsumer 
	 * @Description: TODO
	 * @param @param connection
	 * @param @param queue
	 * @param @return
	 * @return QueueingConsumer
	 * @throws
	 */
	private QueueingConsumer buildQueueConsumer(Connection connection, String queue) {
        try {
            Channel channel = connection.createChannel(false);
            QueueingConsumer consumer = new QueueingConsumer(channel);

            //通过 BasicQos 方法设置prefetchCount = 1。这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
            //换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
            channel.basicQos(1);
            channel.basicConsume(queue, false, consumer);

            return consumer;
        } catch (Exception e) {         
            log.error("build queue consumer error : " + e);
            try {
                Thread.sleep(Constants.ONE_SECOND);
            } catch (InterruptedException inE) {
                inE.printStackTrace();
            }
            return buildQueueConsumer(connection, queue);
        }
    }
}
