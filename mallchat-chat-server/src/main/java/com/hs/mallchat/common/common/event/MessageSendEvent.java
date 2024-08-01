package com.hs.mallchat.common.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description: 消息发送监听器事件
 *
 * @Author: CZF
 * @Create: 2024/7/27 - 19:59
 */
@Getter
public class MessageSendEvent extends ApplicationEvent {

    private Long msgId;

    public MessageSendEvent(Object source, Long msgId) {
        super(source);
        this.msgId = msgId;
    }
}
