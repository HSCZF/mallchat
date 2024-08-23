package com.hs.mallchat.common.user.consumer;

import com.hs.mallchat.common.common.constant.MQConstant;
import com.hs.mallchat.common.common.domain.dto.PushMessageDTO;
import com.hs.mallchat.common.user.domain.enums.WSPushTypeEnum;
import com.hs.mallchat.common.user.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description: 消息消费者
 * RocketMQ消息消费者实现，用于处理特定主题(PUSH_TOPIC)的消息
 * RocketMQ 消费者，只有topic是MQConstant.PUSH_TOPIC一致，用于接收 PUSH_TOPIC 主题的消息。
 * MessageModel.BROADCASTING（广播消费模式）：消息会被消费组中的所有消费者消费。
 * 代码一共有一个MQProducer类是生产者
 * 4个消费者：MsgSendConsumer，MsgLoginConsumer，PushConsumer，ScanSuccessConsumer
 * PushService类做统一的调用
 *
 * @Author: CZF
 * @Create: 2024/7/30 - 16:09
 */
// 使用RocketMQMessageListener注解配置RocketMQ消费者，指定主题、消费组和消息模型（广播模式）
// 使用Spring Component注解表明这是一个组件，以便Spring管理
// 定义PushConsumer类，实现RocketMQListener接口，指定处理的消息类型为PushMessageDTO
@RocketMQMessageListener(consumerGroup = MQConstant.PUSH_GROUP, topic = MQConstant.PUSH_TOPIC, messageModel = MessageModel.BROADCASTING)
@Component
public class PushConsumer implements RocketMQListener<PushMessageDTO> {
    @Autowired
    private WebSocketService webSocketService;

    // 实现RocketMQListener接口的onMessage方法，当接收到消息时调用此方法
    @Override
    public void onMessage(PushMessageDTO message) {
        // 根据消息中的pushType字段获取对应的WSPushTypeEnum枚举值
        WSPushTypeEnum wsPushTypeEnum = WSPushTypeEnum.of(message.getPushType());
        // 使用switch语句根据WSPushTypeEnum的值进行不同的操作
        switch (wsPushTypeEnum) {
            // 如果pushType对应的是个人推送
            case USER:
                // 遍历消息中的uidList，对每个uid发送消息
                message.getUidList().forEach(uid -> {
                    webSocketService.sendToUid(message.getWsBaseMsg(), uid);
                });
                break;
            // 如果pushType对应的是全部用户推送
            case ALL:
                // 调用WebSocketService的sendToAllOnline方法，向所有在线用户发送消息
                webSocketService.sendToAllOnline(message.getWsBaseMsg(), null);
                break;
        }
    }
}

