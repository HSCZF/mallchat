package com.hs.mallchat.common.chat.service.impl;

import com.hs.mallchat.common.chat.dao.GroupMemberDao;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.dao.RoomFriendDao;
import com.hs.mallchat.common.chat.domain.entity.*;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.chat.service.cache.RoomCache;
import com.hs.mallchat.common.chat.service.cache.RoomGroupCache;
import com.hs.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hs.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hs.mallchat.common.common.domain.enums.NormalOrNoEnum;
import com.hs.mallchat.common.common.utils.AssertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // todo 这里先不扩展，后续再改
        AbstractMsgHandler msgHandler = MsgHandlerFactory.getStrategyNoNull(request.getMsgType());
        // todo checkMsg 保存校验还没写
        msgHandler.checkMsg(request, uid);
        // 同步获取消息的跳转链接标题
        Message insert = MessageAdapter.buildMsgSave(request, uid);
        messageDao.save(insert);
        msgHandler.saveMsg(insert, request);

        // 发布消息发送事件
//        ApplicationEventPublisher.publishEvent();
        return insert.getId();
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

}
