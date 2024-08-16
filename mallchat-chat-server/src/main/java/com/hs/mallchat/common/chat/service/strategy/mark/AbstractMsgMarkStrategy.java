package com.hs.mallchat.common.chat.service.strategy.mark;

import com.hs.mallchat.common.chat.dao.MessageMarkDao;
import com.hs.mallchat.common.chat.domain.dto.ChatMessageMarkDTO;
import com.hs.mallchat.common.chat.domain.entity.MessageMark;
import com.hs.mallchat.common.chat.domain.enums.MessageMarkActTypeEnum;
import com.hs.mallchat.common.chat.domain.enums.MessageMarkTypeEnum;
import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hs.mallchat.common.common.event.MessageMarkEvent;
import com.hs.mallchat.common.common.exception.BusinessException;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;

/**
 * Description:
 * 消息标记抽象类
 *
 * @Author: CZF
 * @Create: 2024/8/16 - 14:27
 */
public abstract class AbstractMsgMarkStrategy {

    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    protected abstract MessageMarkTypeEnum getTypeEnum();

    @Transactional
    public void mark(Long uid, Long msgId) {
        doMark(uid, msgId);
    }

    @Transactional
    public void unMark(Long uid, Long msgId) {
        doUnMark(uid, msgId);
    }

    /**
     * 在当前类实例化完成后，注册当前类到消息标记工厂
     * 该方法通过注解表明是一个初始化方法，在对象创建后调用
     * 主要用于将当前类的类型和实例注册到一个全局的工厂中，以便在其他地方通过工厂获取
     * MessageMarkTypeEnum，2个枚举值，分别表示点赞和举报，type=1,2
     * 父类AbstractMsgMarkStrategy，子类DisLikeStrategy，LikeStrategy继承了AbstractMsgMarkStrategy
     * 初始化getTypeEnum().getType()的值会是1，2
     */
    @PostConstruct
    private void init() {
        MsgMarkFactory.register(getTypeEnum().getType(), this);
    }

    protected void doMark(Long uid, Long msgId) {
        exec(uid, msgId, MessageMarkActTypeEnum.MARK);
    }

    protected void doUnMark(Long uid, Long msgId) {
        exec(uid, msgId, MessageMarkActTypeEnum.UN_MARK);
    }

    /**
     * 执行消息标记操作
     * 根据用户ID和消息ID，以及标记操作类型，来对消息标记进行创建或更新
     * 如果尝试对不存在的消息进行取消标记操作，则不进行任何处理
     *
     * @param uid 用户ID
     * @param msgId 消息ID
     * @param actTypeEnum 消息标记操作类型枚举
     */
    protected void exec(Long uid, Long msgId, MessageMarkActTypeEnum actTypeEnum) {
        // 获取当前操作的消息标记类型
        Integer markType = getTypeEnum().getType();
        // 获取消息标记操作类型
        Integer actType = actTypeEnum.getType();
        // 尝试从数据库中获取现有的消息标记
        MessageMark oldMark = messageMarkDao.get(uid, msgId, markType);
        // 如果未找到旧的消息标记且操作类型为取消标记，则直接返回不进行任何操作
        if (Objects.isNull(oldMark) && actTypeEnum == MessageMarkActTypeEnum.UN_MARK) {
            return;
        }
        // 构建新的消息标记对象，用于插入或更新
        MessageMark insertOrUpdate = MessageMark.builder()
                .id(Optional.ofNullable(oldMark).map(MessageMark::getId).orElse(null))
                .uid(uid)
                .msgId(msgId)
                .type(markType)
                .status(transformAct(actType))
                .build();
        // 保存或更新消息标记，并将操作结果赋值给modify变量
        boolean modify = messageMarkDao.saveOrUpdate(insertOrUpdate);
        // 如果消息标记成功保存或更新，则发布消息标记事件
        if (modify) {
            // 构建消息标记事件数据传输对象
            ChatMessageMarkDTO dto = new ChatMessageMarkDTO(uid, msgId, markType, actType);
            // 发布消息标记事件
            applicationEventPublisher.publishEvent(new MessageMarkEvent(this, dto));
        }
    }

    private Integer transformAct(Integer actType) {
        if (actType == 1) {
            return YesOrNoEnum.NO.getStatus();
        } else if (actType == 2) {
            return YesOrNoEnum.YES.getStatus();
        }
        throw new BusinessException("动作类型 1确认 2取消");
    }


}
