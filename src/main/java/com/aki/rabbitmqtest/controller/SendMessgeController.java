package com.aki.rabbitmqtest.controller;

import com.aki.rabbitmqtest.po.SocketMessage;
import com.aki.rabbitmqtest.producer.SoketProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class SendMessgeController {
    @Autowired
    private SoketProducer soketProducer;

    @RequestMapping("testrabbitmq")
    public SocketMessage sendSocket(@RequestParam String content) {
        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setId("testrabbitmq_id");
        socketMessage.setSenderUser("testrabbitmq_suser");
        socketMessage.setReceiverUser("testrabbitmq_ruser");
        socketMessage.setSendTime(new Date());
        socketMessage.setContent(content);
        soketProducer.send(socketMessage);
        return socketMessage;
    }
}
