package com.aki.rabbitmqtest.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 传对象必须序列化
 */
@Data
public class SocketMessage implements Serializable {
    private static final long serialVersionUID = -8221467966772683998L;
    private String id;
    private String senderUser;
    private String receiverUser;
    private String content;
    private Date sendTime;
    private Date readTime;
}
