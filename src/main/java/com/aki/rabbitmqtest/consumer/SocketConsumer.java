package com.aki.rabbitmqtest.consumer;

import com.aki.rabbitmqtest.config.RabbitMQConfig;
import com.aki.rabbitmqtest.po.SocketMessage;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;


/**
 * @Description: 消息消费端
 */
@Component
public class SocketConsumer {

    @RabbitListener(queues = RabbitMQConfig.KEY)
    public void receiveSocket(SocketMessage socketMessage) {
        System.out.println("-----------接收到消息--------");
        System.out.println("消息内容：" + socketMessage.toString());
    }
}
