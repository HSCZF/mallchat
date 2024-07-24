package com.hs.mallchat.common.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.msg.MessageExtra;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.request.msg.TextMsgReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/23 - 8:42
 */
@Component
public class TextMsgHandler extends AbstractMsgHandler<TextMsgReq> {

    @Autowired
    private MessageDao messageDao;

    /**
     * 消息类型
     */
    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    /**
     * 在各个子类实现
     *
     * @param body
     * @param roomId
     * @param uid
     */
    @Override
    protected void checkMsg(TextMsgReq body, Long roomId, Long uid) {
        // todo 暂时不写
    }

    /**
     * 保存消息
     *
     * @param msg
     * @param body
     */
    @Override
    public void saveMsg(Message msg, TextMsgReq body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        // todo 敏感词后续再做
        update.setContent(body.getContent());
        update.setExtra(extra);
        // todo 如果有回复消息，后续再做
        // todo 判断消息url跳转，后续再做
        // todo 艾特功能，后续再做
        messageDao.updateById(update);
    }

    /**
     * 展示消息
     *
     * @param msg
     */
    @Override
    public Object showMsg(Message msg) {
        return null;
    }

    /**
     * 被回复时-展示的消息
     *
     * @param msg
     */
    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    /**
     * 会话列表-展示的消息
     *
     * @param msg
     */
    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}
