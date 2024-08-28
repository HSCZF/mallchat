package com.hs.mallchat.common.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.hs.mallchat.common.chat.dao.*;
import com.hs.mallchat.common.chat.domain.dto.MsgReadInfoDTO;
import com.hs.mallchat.common.chat.domain.entity.*;
import com.hs.mallchat.common.chat.domain.enums.GroupRoleEnum;
import com.hs.mallchat.common.chat.domain.enums.MessageMarkActTypeEnum;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.*;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMemberStatisticResp;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageReadResp;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageResp;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.ContactService;
import com.hs.mallchat.common.chat.service.adapter.MemberAdapter;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.chat.service.adapter.RoomAdapter;
import com.hs.mallchat.common.chat.service.cache.RoomCache;
import com.hs.mallchat.common.chat.service.cache.RoomGroupCache;
import com.hs.mallchat.common.chat.service.helper.ChatMemberHelper;
import com.hs.mallchat.common.chat.service.strategy.mark.AbstractMsgMarkStrategy;
import com.hs.mallchat.common.chat.service.strategy.mark.MsgMarkFactory;
import com.hs.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hs.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hs.mallchat.common.chat.service.strategy.msg.RecallMsgHandler;
import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.domain.enums.NormalOrNoEnum;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.event.MessageSendEvent;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.ChatActiveStatusEnum;
import com.hs.mallchat.common.user.domain.enums.RoleEnum;
import com.hs.mallchat.common.user.domain.vo.response.ws.ChatMemberResp;
import com.hs.mallchat.common.user.service.IRoleService;
import com.hs.mallchat.common.user.service.cache.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
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
    @Autowired
    private ContactService contactService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomGroupDao roomGroupDao;


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
            // bug修复：下面一行代码的第一个参数是roomGroup.getId()而不是roomGroup.getRoomId()
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
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
        // 这个bug解决改造，@Async 异步处理的数据没有那么快
        // 这段if代码主要解决创建群组后的bug，创建群组后，系统会发送一条消息，但是contact表中的lastMsgId为null，导致后面获取消息列表时，会报错
        // contact的表需要经过GroupMemberAddListener的sendAddMsg方法
        // GroupMemberAddListener.sendAddMsg()->chatService.sendMsg()->applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId))
        // ->MessageSendListener.messageRoute()->mqProducer发送topic名称为MQConstant.SEND_MSG_TOPIC，被MsgSendConsumerMQ消费者消费
        // ->contactDao.refreshOrCreateActiveTime()进行执行数据库操作，此时新增群组的会话才有数据，所以contact.getLastMsgId()会null，就报错
        // 解决方案：1、GroupMemberAddListener.sendAddMsg()方法改为同步执行
        //         2、这里判断contact是不是null
        if (Objects.isNull(contact)) {
            Message message = messageDao.getByUidAndRoomId(roomId, User.UID_SYSTEM);
            return message.getId();
        } else if (Objects.isNull(contact.getLastMsgId())) {
            // contact 不为 null，再检查 lastMsgId 是否为 null
            Message message = messageDao.getByUidAndRoomId(roomId, User.UID_SYSTEM);
            return message.getId();
        }
        // contact 不为 null，且 lastMsgId 不为 null，则直接返回 lastMsgId
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
    @RedissonLock(key = "#uid")
    public void setMsgMark(Long uid, ChatMessageMarkReq request) {
        AbstractMsgMarkStrategy strategy = MsgMarkFactory.getStrategyNoNull(request.getMarkType());
        switch (MessageMarkActTypeEnum.of(request.getActType())) {
            case MARK:
                strategy.mark(uid, request.getMsgId());
                break;
            case UN_MARK:
                strategy.unMark(uid, request.getMsgId());
                break;
        }
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

    @Override
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(@Nullable Long uid, ChatMessageReadReq request) {
        Message message = messageDao.getById(request.getMsgId());
        AssertUtil.isNotEmpty(message, "消息id有误");
        AssertUtil.equal(uid, message.getFromUid(), "只能查看自己的消息");
        CursorPageBaseResp<Contact> page;
        if (request.getSearchType() == 1) { //已读
            page = contactDao.getReadPage(message, request);
        } else {
            page = contactDao.getUnReadPage(message, request);
        }
        if (CollectionUtil.isEmpty(page.getList())) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(page, RoomAdapter.buildReadResp(page.getList()));
    }

    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request) {
        List<Message> messages = messageDao.listByIds(request.getMsgIds());
        messages.forEach(message -> {
            AssertUtil.equal(uid, message.getFromUid(), "只能查询自己发送的消息");
        });
        return contactService.getMsgReadInfo(messages).values();
    }

    @Override
    @RedissonLock(key = "#uid")
    public void msgRead(Long uid, ChatMessageMemberReq request) {
        Contact contact = contactDao.get(uid, request.getRoomId());
        if (Objects.nonNull(contact)) {
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(new Date());
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(request.getRoomId());
            insert.setReadTime(new Date());
            contactDao.save(insert);
        }
    }

    /**
     * 获取群成员列表
     *
     * @param memberUidList 群成员UID列表
     * @param request       查询请求
     * @return 返回群成员列表的游标分页响应
     */
    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request) {
        // 解析游标
        Pair<ChatActiveStatusEnum, String> pair = ChatMemberHelper.getCursorPair(request.getCursor());
        ChatActiveStatusEnum activeStatusEnum = pair.getKey();
        String timeCursor = pair.getValue();
        List<ChatMemberResp> resultList = new ArrayList<>();
        Boolean isLast = Boolean.FALSE;

        if (activeStatusEnum == ChatActiveStatusEnum.ONLINE) {
            // 获取在线用户分页数据
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.ONLINE);
            // 在线列表
            resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));
            if (cursorPage.getIsLast()) { // 如果是最后一页,从离线列表再补一点数据
                activeStatusEnum = ChatActiveStatusEnum.OFFLINE;
                Integer leftSize = request.getPageSize() - cursorPage.getList().size();
                cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(leftSize, null), ChatActiveStatusEnum.OFFLINE);
                // 添加离线列表
                resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));
            }
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        } else if (activeStatusEnum == ChatActiveStatusEnum.OFFLINE) {
            // 获取离线用户分页数据
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.OFFLINE);
            resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        }

        // 获取群成员角色ID
        List<Long> uidList = resultList.stream().map(ChatMemberResp::getUid).collect(Collectors.toList());
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        Map<Long, Integer> uidMapRole = groupMemberDao.getMemberMapRole(roomGroup.getId(), uidList);
        resultList.forEach(member -> member.setRoleId(uidMapRole.get(member.getUid())));

        // 组装结果
        return new CursorPageBaseResp<>(ChatMemberHelper.generateCursor(activeStatusEnum, timeCursor), isLast, resultList);
    }

}
