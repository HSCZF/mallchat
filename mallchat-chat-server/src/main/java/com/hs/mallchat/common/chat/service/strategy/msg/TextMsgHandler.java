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
public class TextMsgHandler extends AbstractMsgHandler {

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
     * 校验消息-保存前校验
     *
     * @param req
     * @param uid
     */
    @Override
    public void checkMsg(ChatMessageReq req, Long uid) {

    }

    /**
     * 保存消息
     *
     * @param msg
     * @param request
     */
    @Override
    public void saveMsg(Message msg, ChatMessageReq request) {
        TextMsgReq body = BeanUtil.toBean(request.getBody(), TextMsgReq.class);
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
        return null;
    }

    /**
     * 会话列表-展示的消息
     *
     * @param msg
     */
    @Override
    public Object showContactMsg(Message msg) {
        return null;
    }
}
