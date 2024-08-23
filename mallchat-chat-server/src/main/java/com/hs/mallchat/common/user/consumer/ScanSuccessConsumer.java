package com.hs.mallchat.common.user.consumer;

import com.hs.mallchat.common.common.constant.MQConstant;
import com.hs.mallchat.common.common.domain.dto.ScanSuccessMessageDTO;
import com.hs.mallchat.common.user.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:  将扫码成功的信息发送给对应的用户,等待授权
 * RocketMQ 消费者，只有topic是MQConstant.SCAN_MSG_TOPIC一致，用于接收 SCAN_MSG_TOPIC 主题的消息。
 * MessageModel.BROADCASTING（广播消费模式）：消息会被消费组中的所有消费者消费。
 * 代码一共有一个MQProducer类是生产者
 * 4个消费者：MsgSendConsumer，MsgLoginConsumer，PushConsumer，ScanSuccessConsumer
 * PushService类做统一的调用
 *
 * @Author: CZF
 * @Create: 2024/7/31 - 16:17
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SCAN_MSG_GROUP, topic = MQConstant.SCAN_MSG_TOPIC, messageModel = MessageModel.BROADCASTING)
@Component
public class ScanSuccessConsumer implements RocketMQListener<ScanSuccessMessageDTO> {
    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void onMessage(ScanSuccessMessageDTO scanSuccessMessageDTO) {
        webSocketService.scanSuccess(scanSuccessMessageDTO.getCode());
    }

}
