package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.chat.dao.GroupMemberDao;
import com.hs.mallchat.common.chat.domain.entity.GroupMember;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.adapter.MemberAdapter;
import com.hs.mallchat.common.chat.service.adapter.RoomAdapter;
import com.hs.mallchat.common.chat.service.cache.GroupMemberCache;
import com.hs.mallchat.common.chat.service.cache.MsgCache;
import com.hs.mallchat.common.common.event.GroupMemberAddEvent;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBaseResp;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSMemberChange;
import com.hs.mallchat.common.user.service.cache.UserInfoCache;
import com.hs.mallchat.common.user.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * 添加群成员监听器
 *
 * @Author: CZF
 * @Create: 2024/8/23 - 16:32
 */
@Slf4j
@Component
public class GroupMemberAddListener {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;


    //@Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        Long inviteUid = event.getInviteUid();
        User user = userInfoCache.get(inviteUid);
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        ChatMessageReq chatMessageReq = RoomAdapter.buildGroupAddMessage(roomGroup, user, userInfoCache.getBatch(uidList));
        chatService.sendMsg(chatMessageReq, User.UID_SYSTEM);
    }

    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WSBaseResp<WSMemberChange> ws =
                    MemberAdapter.buildMemberAddWS(roomGroup.getRoomId(), user);
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
    }

}

