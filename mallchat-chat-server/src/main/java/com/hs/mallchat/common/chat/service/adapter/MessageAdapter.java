package com.hs.mallchat.common.chat.service.adapter;

import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.enums.MessageStatusEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;

/**
 * Description: 消息适配器
 *
 * @Author: CZF
 * @Create: 2024/7/23 - 8:23
 */
public class MessageAdapter {

    public static final int CAN_CALLBACK_GAP_COUNT = 100;


    public static Message buildMsgSave(ChatMessageReq request, Long uid) {
        return Message.builder()
                .fromUid(uid)
                .roomId(request.getRoomId())
                .type(request.getMsgType())
                .status(MessageStatusEnum.NORMAL.getStatus())
                .build();
    }
}
