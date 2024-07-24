package com.hs.mallchat.common.chat.service.strategy.msg;

import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.msg.EmojisMsgDTO;
import com.hs.mallchat.common.chat.domain.entity.msg.MessageExtra;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description: 表情消息
 *
 * @Author: CZF
 * @Create: 2024/7/23 - 21:16
 */
@Component
public class EmojisMsgHandler extends AbstractMsgHandler<EmojisMsgDTO> {

    @Autowired
    private MessageDao messageDao;

    /**
     * 获取消息类型枚举。
     * 必须由子类实现以指定消息的类型。
     *
     * @return 消息类型枚举值
     */
    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.EMOJI;
    }


    /**
     * 保存消息的额外处理。
     * 由子类实现，用于执行保存消息后的特定操作。
     *
     * @param msg  消息实体
     * @param body 消息请求体
     */
    @Override
    public void saveMsg(Message msg, EmojisMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setEmojisMsgDTO(body);
        messageDao.updateById(update);
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
        return msg.getExtra().getEmojisMsgDTO();
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
        return "表情";
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
        return "[表情包]";
    }
}
