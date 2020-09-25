package com.aki.rabbitmqtest.consumer;

import com.aki.rabbitmqtest.po.SocketMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


/**
 * @Description: 消息消费端
 */
@Component
public class SocketConsumer2 {
    @RabbitListener(queues = "routingKey")
    public void receiveSocket(SocketMessage socketMessage) {
        System.out.println("-----------2 接收到消息--------");
        System.out.println("2 消息内容：" + socketMessage.toString());
    }
}
