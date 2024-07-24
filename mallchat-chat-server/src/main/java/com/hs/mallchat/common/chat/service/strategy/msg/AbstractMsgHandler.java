package com.hs.mallchat.common.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.msg.MessageExtra;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.request.msg.TextMsgReq;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * 抽象消息处理器基类，用于处理不同类型的聊天消息。
 * 通过泛型 `<Req>` 支持不同的消息请求体类型，实现消息的动态处理。
 *
 * @param <Req> 具体消息请求体的类型
 * @author CZF
 * @since 2024/7/22
 */
public abstract class AbstractMsgHandler<Req> {

    @Autowired
    private MessageDao messageDao;

    private Class<Req> bodyClass; // 存储泛型的实际类型信息

    /**
     * 初始化方法，在实例创建后自动调用。
     * 注册当前消息处理器至消息处理器工厂。
     */
    @PostConstruct
    private void init() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.bodyClass = (Class<Req>) genericSuperclass.getActualTypeArguments()[0];
        MsgHandlerFactory.register(getMsgTypeEnum().getType(), this);
    }

    /**
     * 获取消息类型枚举。
     * 必须由子类实现以指定消息的类型。
     *
     * @return 消息类型枚举值
     */
    abstract MessageTypeEnum getMsgTypeEnum();

    /**
     * 验证消息内容。
     * 由子类实现，用于对消息内容进行特定的检查。
     *
     * @param body   消息请求体
     * @param roomId 房间ID
     * @param uid    用户ID
     */
    protected void checkMsg(Req body, Long roomId, Long uid) {
    }

    /**
     * 验证并保存消息。
     *
     * @param request 消息请求
     * @param uid     用户ID
     * @return 保存的消息ID
     * @Transactional 确保事务一致性。
     */
    @Transactional
    public Long checkAndSaveMsg(ChatMessageReq request, Long uid) {
        Req body = this.toBean(request.getBody());
        // 统一校验
        AssertUtil.allCheckValidateThrow(body);
        // 子类扩展校验
        checkMsg(body, request.getRoomId(), uid);
        Message insert = MessageAdapter.buildMsgSave(request, uid);
        // 统一保存
        messageDao.save(insert);
        // 子类扩展保存
        saveMsg(insert, body);
        return insert.getId();
    }

    /**
     * 将对象转换为泛型类型。
     *
     * @param body 原始消息体
     * @return 转换后的泛型消息体
     */
    private Req toBean(Object body) {
        if (bodyClass.isAssignableFrom(body.getClass())) {
            return (Req) body;
        }
        return BeanUtil.toBean(body, bodyClass);
    }

    /**
     * 保存消息的额外处理。
     * 由子类实现，用于执行保存消息后的特定操作。
     *
     * @param msg  消息实体
     * @param body 消息请求体
     */
    protected abstract void saveMsg(Message msg, Req body);

    /**
     * 展示消息。
     * 由子类实现，用于消息的展示逻辑。
     *
     * @param msg 消息实体
     * @return 展示的消息内容
     */
    public abstract Object showMsg(Message msg);

    /**
     * 展示被回复的消息。
     * 由子类实现，用于展示作为回复目标的消息。
     *
     * @param msg 消息实体
     * @return 展示的被回复消息内容
     */
    public abstract Object showReplyMsg(Message msg);

    /**
     * 展示会话列表中的消息。
     * 由子类实现，用于会话列表中消息的展示逻辑。
     *
     * @param msg 消息实体
     * @return 展示的会话列表消息内容
     */
    public abstract String showContactMsg(Message msg);
}
