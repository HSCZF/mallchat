package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.constant.MQConstant;
import com.hs.mallchat.common.common.domain.dto.PushMessageDTO;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBaseResp;
import com.hs.mallchat.transaction.service.MQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/29 - 16:04
 */

// 定义一个服务层的bean，用于处理消息推送相关业务
@Service
public class PushService {

    // 自动装配MQ生产者实例，用于发送消息
    @Autowired
    private MQProducer mqProducer;

    /**
     * 向指定的一组用户发送推送消息
     *
     * @param msg 要发送的消息内容
     * @param uidList 接收消息的用户ID列表
     */
    public void sendPushMsg(WSBaseResp<?> msg, List<Long> uidList) {
        // 构造推送消息对象，包含用户列表和消息内容，然后发送到指定的主题
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC, new PushMessageDTO(uidList, msg));
    }

    /**
     * 向指定的单个用户发送推送消息
     *
     * @param msg 要发送的消息内容
     * @param uid 接收消息的单一用户ID
     */
    public void sendPushMsg(WSBaseResp<?> msg, Long uid) {
        // 构造推送消息对象，包含单个用户ID和消息内容，然后发送到指定的主题
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC, new PushMessageDTO(uid, msg));
    }

    /**
     * 发送无特定接收者的推送消息，通常用于广播类型的消息
     *
     * @param msg 要发送的消息内容
     */
    public void sendPushMsg(WSBaseResp<?> msg) {
        // 构造仅包含消息内容的推送消息对象，没有指定接收者，然后发送到指定的主题
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC, new PushMessageDTO(msg));
    }

}
