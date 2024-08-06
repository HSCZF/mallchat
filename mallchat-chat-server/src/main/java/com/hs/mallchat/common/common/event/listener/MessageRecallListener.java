package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.chat.domain.dto.ChatMsgRecallDTO;
import com.hs.mallchat.common.chat.service.cache.MsgCache;
import com.hs.mallchat.common.common.event.MessageRecallEvent;
import com.hs.mallchat.common.user.service.adapter.WSAdapter;
import com.hs.mallchat.common.user.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Description: 消息撤回监听器
 *
 * @Author: CZF
 * @Create: 2024/8/6 - 10:54
 */
@Slf4j
@Component
public class MessageRecallListener {

    @Autowired
    private PushService pushService;
    @Autowired
    private MsgCache msgCache;

    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void evictMsg(MessageRecallEvent event) {
        ChatMsgRecallDTO recallDTO = event.getRecallDTO();
        msgCache.evictMsg(recallDTO.getMsgId());
    }

    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void sendToAll(MessageRecallEvent event) {
        pushService.sendPushMsg(WSAdapter.buildMsgRecall(event.getRecallDTO()));
    }

}
