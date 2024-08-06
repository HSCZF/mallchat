package com.hs.mallchat.common.chat.service.strategy.msg;

import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.dto.ChatMsgRecallDTO;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.msg.FileMsgDTO;
import com.hs.mallchat.common.chat.domain.entity.msg.MessageExtra;
import com.hs.mallchat.common.chat.domain.entity.msg.MsgRecall;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.common.event.MessageRecallEvent;
import com.hs.mallchat.common.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * Description: 撤回文本消息
 *
 * @Author: CZF
 * @Create: 2024/7/23 - 21:16
 */
@Component
public class RecallMsgHandler extends AbstractMsgHandler<Object> {

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 获取消息类型枚举。
     * 必须由子类实现以指定消息的类型。
     *
     * @return 消息类型枚举值
     */
    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.RECALL;
    }

    /**
     * 保存消息的额外处理。
     * 由子类实现，用于执行保存消息后的特定操作。
     *
     * @param msg  消息实体
     * @param body 消息请求体
     */
    @Override
    public void saveMsg(Message msg, Object body) {
        throw new UnsupportedOperationException();
    }


    /**
     * 展示消息。
     * 由子类实现，用于消息的展示逻辑。
     *
     * @param msg 消息实体
     * @return 展示的消息内容
     */
    @Override
    public Object showMsg(Message msg) {
        // TODO: 撤回消息，待实现
        return null;
    }

    /**
     * 展示被回复的消息。
     * 由子类实现，用于展示作为回复目标的消息。
     *
     * @param msg 消息实体
     * @return 展示的被回复消息内容
     */
    @Override
    public Object showReplyMsg(Message msg) {
        return "原消息已被撤回";
    }

    public void recall(Long recallUid, Message message) {
        //todo 消息覆盖问题用版本号解决
        MessageExtra extra = message.getExtra();
        extra.setRecall(new MsgRecall(recallUid, new Date()));
        Message update = new Message();
        update.setId(message.getId());
        update.setType(MessageTypeEnum.RECALL.getType());
        update.setExtra(extra);
        messageDao.updateById(update);
        applicationEventPublisher.publishEvent(new MessageRecallEvent(this, new ChatMsgRecallDTO(message.getId(), message.getRoomId(), recallUid)));
    }

    /**
     * 展示会话列表中的消息。
     * 由子类实现，用于会话列表中消息的展示逻辑。
     *
     * @param msg 消息实体
     * @return 展示的会话列表消息内容
     */
    @Override
    public String showContactMsg(Message msg) {
        return "撤回了一条消息";
    }
}
