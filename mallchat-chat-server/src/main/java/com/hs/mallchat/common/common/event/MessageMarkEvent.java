package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.chat.domain.dto.ChatMessageMarkDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/16 - 14:42
 */
@Getter
public class MessageMarkEvent extends ApplicationEvent {

    private final ChatMessageMarkDTO dto;

    public MessageMarkEvent(Object source, ChatMessageMarkDTO dto) {
        super(source);
        this.dto = dto;
    }
}
