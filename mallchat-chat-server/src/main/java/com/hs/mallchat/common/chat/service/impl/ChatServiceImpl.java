package com.hs.mallchat.common.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.hs.mallchat.common.chat.dao.*;
import com.hs.mallchat.common.chat.domain.entity.*;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageBaseReq;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessagePageReq;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMemberStatisticResp;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageResp;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.chat.service.cache.RoomCache;
import com.hs.mallchat.common.chat.service.cache.RoomGroupCache;
import com.hs.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hs.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hs.mallchat.common.chat.service.strategy.msg.RecallMsgHandler;
import com.hs.mallchat.common.common.domain.enums.NormalOrNoEnum;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.event.MessageSendEvent;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.domain.enums.RoleEnum;
import com.hs.mallchat.common.user.service.IRoleService;
import com.hs.mallchat.common.user.service.cache.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 20:18
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private UserCache userCache;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private IRoleService iRoleService;
    @Autowired
    private RecallMsgHandler recallMsgHandler;


    /**
     * 发送消息
     *
     * @param request
     * @param uid
     * @return
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        check(request, uid);
        // 优化，改泛型
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(request.getMsgType());
        // 保存校验
        Long msgId = msgHandler.checkAndSaveMsg(request, uid);
        // 发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId));
        return msgId;
    }

    private void check(ChatMessageReq request, Long uid) {
        Room room = roomCache.get(request.getRoomId());
        if (room.isHotRoom()) { //全员群跳过校验
            return;
        }
        if (room.isRoomFriend()) {
            RoomFriend roomFriend = roomFriendDao.getByRoomId(request.getRoomId());
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            GroupMember member = groupMemberDao.getMember(roomGroup.getRoomId(), uid);
            AssertUtil.isNotEmpty(member, "您已经被移除该群");
        }
    }

    /**
     * 根据消息和接收者UID获取单条消息响应。
     * 此方法通过调用getMsgRespBatch方法来批量处理消息响应，尽管在此处只处理单条消息。
     * 如果接收者UID为null，消息将被默认处理，不受特定接收者影响。
     *
     * @param message    需要处理的消息对象。
     * @param receiveUid 消息的接收者UID，可能为null。
     * @return 返回处理后的聊天消息响应对象。
     */
    @Override
    public ChatMessageResp getMsgResp(Message message, Long receiveUid) {
        // 通过调用getMsgRespBatch方法处理单条消息，并从处理结果中获取第一条消息响应。
        // CollUtil.getFirst()用于从集合中获取第一个元素。如果集合为空，它通常会返回 null 或者一个默认值
        // Collections.singletonList 是 Java 集合框架中的一个静态工厂方法，它被定义在 java.util.Collections 类中。这个方法用于创建一个固定大小的列表，这个列表只能包含一个元素，
        // 并且是不可变的（immutable）。这意味着一旦你通过 singletonList 创建了一个列表，你就不能向其中添加、删除或修改元素。
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(message), receiveUid));
    }

    /**
     * 获取消息列表
     *
     * @param request
     * @param receiveUid
     * @return
     */
    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, Long receiveUid) {
        // 用最后一条消息的id，来限制被踢出的人能看见的最大一条消息
        Long lastMsgId = getLastMsgId(request.getRoomId(), receiveUid);
        CursorPageBaseResp<Message> cursorPage = messageDao.getCursorPage(request.getRoomId(), request, lastMsgId);
        if (cursorPage.isEmpty()) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(cursorPage, getMsgRespBatch(cursorPage.getList(), receiveUid));
    }

    private Long getLastMsgId(Long roomId, Long receiveUid) {
        // 在抽象类AbstractRedisStringCache和子类共同实现添加到redis缓存的操作
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (room.isHotRoom()) {
            return null;
        }
        AssertUtil.isNotEmpty(receiveUid, "请先登录");
        Contact contact = contactDao.get(receiveUid, roomId);
        return contact.getLastMsgId();
    }

    /**
     * 根据消息获取消息前端展示的物料
     *
     * @param msgId
     * @param receiveUid 接受消息的uid，可null
     * @return
     */
    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long receiveUid) {
        Message msg = messageDao.getById(msgId);
        return getMsgResp(msg, receiveUid);
    }

    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq request) {
        Message message = messageDao.getById(request.getMsgId());
        // 校验能不能执行撤回
        checkRecall(uid, message);
        // 执行消息撤回
        recallMsgHandler.recall(uid, message);
    }

    private void checkRecall(Long uid, Message message) {
        AssertUtil.isNotEmpty(message, "消息有误");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息无法撤回");
        boolean hasPower = iRoleService.hasPower(uid, RoleEnum.CHAT_MANAGER);
        if (hasPower) {
            return;
        }
        boolean self = Objects.equals(uid, message.getFromUid());
        AssertUtil.isTrue(self, "抱歉,您没有权限");
        long between = DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between < 2, "超过2分钟的消息不能撤回");
    }

    @Override
    public ChatMemberStatisticResp getMemberStatistic() {
        log.info("ChatServiceImpl-getMemberStatistic()：[]" + Thread.currentThread().getName());
        Long onlineNum = userCache.getOnlineNum();
        ChatMemberStatisticResp resp = new ChatMemberStatisticResp();
        resp.setOnlineNum(onlineNum);
        return resp;
    }

    /**
     * 批量获取消息的响应对象列表。
     * 此方法用于根据一系列消息ID和接收用户的ID，查询并构建对应的消息响应对象列表。
     * 主要包括了消息标记的查询和消息对象的构建两个步骤。
     *
     * @param messages   消息列表，用于查询的消息对象集合。
     * @param receiveUid 接收用户的ID，用于指定消息的接收方。
     * @return 返回一个消息响应对象的列表。如果输入的消息列表为空，则返回空列表。
     */
    private List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long receiveUid) {
        // 检查消息列表是否为空，如果为空则直接返回空列表。
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        // 根据消息列表中的消息ID，批量查询对应的消息标记。
        // 查询消息标志
        List<MessageMark> msgMark = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        // 使用查询到的消息标记和原始消息列表，以及接收用户的ID，构建消息响应对象列表。
        return MessageAdapter.buildMsgSave(messages, msgMark, receiveUid);
    }
}
