package com.hs.mallchat.common.chat.service.impl;

import com.hs.mallchat.common.chat.dao.*;
import com.hs.mallchat.common.chat.domain.entity.Contact;
import com.hs.mallchat.common.chat.domain.entity.GroupMember;
import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberExitReq;
import com.hs.mallchat.common.chat.service.IGroupMemberService;
import com.hs.mallchat.common.chat.service.adapter.MemberAdapter;
import com.hs.mallchat.common.chat.service.cache.GroupMemberCache;
import com.hs.mallchat.common.common.exception.CommonErrorEnum;
import com.hs.mallchat.common.common.exception.GroupErrorEnum;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBaseResp;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSMemberChange;
import com.hs.mallchat.common.user.service.impl.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 * 群成员服务类
 *
 * @Author: CZF
 * @Create: 2024/8/21 - 21:04
 */
@Service
public class GroupMemberServiceImpl implements IGroupMemberService {

    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private RoomGroupDao roomGroupDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private ContactDao contactDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private GroupMemberCache groupMemberCache;

    @Autowired
    private PushService pushService;

    /**
     * 退出群聊
     *
     * @param uid     用户ID
     * @param request 请求信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitGroup(Long uid, MemberExitReq request) {
        Long roomId = request.getRoomId();
        // 1、判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);
        // 2、判断房间是否为大群聊，大群聊禁止退出
        Room room = roomDao.getById(roomId);
        AssertUtil.isNotEmpty(room.isHotRoom(), GroupErrorEnum.NOT_ALLOWED_FOR_EXIT_GROUP);
        // 3、判断群成员是否在群中,先去缓存中去查询，找不到再去数据库查询
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), Collections.singletonList(uid));
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);
        // 4、判断是不是群主
        boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        if (isLord) {
            // 是群主
            // 4.1 删除房间
            boolean isDelRoom = roomDao.removeById(roomId);
            AssertUtil.isTrue(isDelRoom, CommonErrorEnum.SYSTEM_ERROR);
            // 4.2 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, Collections.EMPTY_LIST);
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.3 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.EMPTY_LIST);
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.4 删除群组(直接删除)
            Boolean isDelRoomGroup = roomGroupDao.deleteByRoomId(roomGroup.getId());
            AssertUtil.isTrue(isDelRoomGroup, CommonErrorEnum.SYSTEM_ERROR);
            // 4.5 删除消息记录 (逻辑删除)
            Boolean isDelMessage = messageDao.removeByRoomId(roomId, Collections.EMPTY_LIST);
            AssertUtil.isTrue(isDelMessage, CommonErrorEnum.SYSTEM_ERROR);
        } else {
            // 不是群主，退出群
            // 4.6 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, Collections.singletonList(uid));
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.7 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.singletonList(uid));
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.8 发送移除事件告知群成员
            List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), uid);
            pushService.sendPushMsg(ws, memberUidList);
            // 清理这个房间的缓存数据
            groupMemberCache.evictMemberUidList(room.getId());
        }

    }
}