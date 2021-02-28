package com.aki.rabbitmqtest.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置
 * 交换机有四种类型,分别为Direct,topic,headers,Fanout
 */
@Configuration
public class RabbitMQConfig {
    public static final String KEY = "routingKey";

    // Direct 模式创建队列
    // 创建队列 生产者消费者都得写 不然报错
    @Bean
    public Queue directQueue() {
        return new Queue(KEY);  // 队列名和routingKey相同
    }
}
