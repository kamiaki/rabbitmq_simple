package com.aki.rabbitmqtest.producer;

import com.aki.rabbitmqtest.po.SocketMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 消息生产者
 */
@Component
public class SoketProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(SocketMessage socketMessage) {
        // 没有交换机名称，消息会被发送到默认交换机，然后被转发到 名称和routingKey相同的队列上
        // 传对象必须序列化
        rabbitTemplate.convertAndSend("routingKey", socketMessage);
        System.out.println("确认结束");
    }
}
