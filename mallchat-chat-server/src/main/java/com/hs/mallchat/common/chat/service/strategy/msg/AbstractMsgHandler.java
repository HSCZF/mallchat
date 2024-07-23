package com.hs.mallchat.common.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Description: 消息处理器抽象类
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 21:04
 */
public abstract class AbstractMsgHandler {

    @PostConstruct
    private void init() {
        MsgHandlerFactory.register(getMsgTypeEnum().getType(), this);
    }

    /**
     * 消息类型
     */
    abstract MessageTypeEnum getMsgTypeEnum();

    /**
     * 校验消息-保存前校验
     */
    public abstract void checkMsg(ChatMessageReq req, Long uid);


    /**
     * 保存消息
     */
    public abstract void saveMsg(Message msg, ChatMessageReq req);

    /**
     * 展示消息
     */
    public abstract Object showMsg(Message msg);

    /**
     * 被回复时-展示的消息
     */
    public abstract Object showReplyMsg(Message msg);

    /**
     * 会话列表-展示的消息
     */
    public abstract Object showContactMsg(Message msg);


}
