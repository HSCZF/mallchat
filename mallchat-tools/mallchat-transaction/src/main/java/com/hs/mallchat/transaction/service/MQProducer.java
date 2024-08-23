package com.hs.mallchat.transaction.service;


import com.hs.mallchat.transaction.annotation.SecureInvoke;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Description: 发送mq工具类
 * RocketMQ 生产者，发送到不同的主题，消费者根据对应的主题进行消费
 * 代码一共有一个MQProducer类是生产者
 * 4个消费者：MsgSendConsumer，MsgLoginConsumer，PushConsumer，ScanSuccessConsumer
 * PushService类做统一的调用
 * @Author: CZF
 * @Create: 2024/7/24 - 11:04
 */
public class MQProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendMsg(String topic, Object body) {
        Message<Object> build = MessageBuilder.withPayload(body).build();
        rocketMQTemplate.send(topic, build);
    }

    /**
     * 发送可靠消息，在事务提交后保证发送成功
     *
     * @param topic
     * @param body
     */
    @SecureInvoke
    public void sendSecureMsg(String topic, Object body, Object key) {
        Message<Object> build = MessageBuilder
                .withPayload(body)
                .setHeader("KEYS", key)
                .build();
        rocketMQTemplate.send(topic, build);
    }

}
