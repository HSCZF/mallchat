package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.chat.domain.enums.HotFlagEnum;
import com.hs.mallchat.common.chat.service.cache.RoomCache;
import com.hs.mallchat.common.common.constant.MQConstant;
import com.hs.mallchat.common.common.domain.dto.MsgSendMessageDTO;
import com.hs.mallchat.common.common.event.MessageSendEvent;
import com.hs.mallchat.transaction.service.MQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Description: 消息发送监听器
 *
 * @Author: CZF
 * @Create: 2024/7/27 - 20:00
 */
@Slf4j
@Component
public class MessageSendListener {

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private MQProducer mqProducer;

    /**
     * 此方法是一个事务监听器，专门处理在事务提交前的消息路由逻辑。
     *
     * @param event 由系统触发的MessageSendEvent事件，携带了需要发送消息的信息。
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT,
            classes = MessageSendEvent.class,
            fallbackExecution = true)
    public void messageRoute(MessageSendEvent event) {
        // 从事件中提取消息ID
        Long msgId = event.getMsgId();

        // 使用消息生产者发送安全消息，目标主题为SEND_MSG_TOPIC，
        // 消息体为包含msgId的MsgSendMessageDTO对象，同时使用msgId作为消息的唯一标识。
        mqProducer.sendSecureMsg(MQConstant.SEND_MSG_TOPIC, new MsgSendMessageDTO(msgId), msgId);
    }

    public void handlerMsg(@NotNull MessageSendEvent event){
        Message message = messageDao.getById(event.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        if(isHotRoom(room)){
            // todo openAi的聊天，暂时不写
        }
    }

    public boolean isHotRoom(Room room) {
        return Objects.equals(HotFlagEnum.YES.getType(), room.getHotFlag());
    }


}
