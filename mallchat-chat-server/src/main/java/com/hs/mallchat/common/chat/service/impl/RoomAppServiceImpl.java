package com.hs.mallchat.common.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import com.hs.mallchat.common.chat.dao.ContactDao;
import com.hs.mallchat.common.chat.dao.GroupMemberDao;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.dto.RoomBaseInfo;
import com.hs.mallchat.common.chat.domain.entity.*;
import com.hs.mallchat.common.chat.domain.enums.GroupRoleAPPEnum;
import com.hs.mallchat.common.chat.domain.enums.GroupRoleEnum;
import com.hs.mallchat.common.chat.domain.enums.HotFlagEnum;
import com.hs.mallchat.common.chat.domain.enums.RoomTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageMemberReq;
import com.hs.mallchat.common.chat.domain.vo.request.GroupAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.MemberDelReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMemberListResp;
import com.hs.mallchat.common.chat.domain.vo.response.ChatRoomResp;
import com.hs.mallchat.common.chat.domain.vo.response.MemberResp;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.RoomAppService;
import com.hs.mallchat.common.chat.service.RoomService;
import com.hs.mallchat.common.chat.service.adapter.ChatAdapter;
import com.hs.mallchat.common.chat.service.adapter.MemberAdapter;
import com.hs.mallchat.common.chat.service.adapter.RoomAdapter;
import com.hs.mallchat.common.chat.service.cache.*;
import com.hs.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hs.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.event.GroupMemberAddEvent;
import com.hs.mallchat.common.common.exception.GroupErrorEnum;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.RoleEnum;
import com.hs.mallchat.common.user.domain.vo.response.ws.ChatMemberResp;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBaseResp;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSMemberChange;
import com.hs.mallchat.common.user.service.IRoleService;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.user.service.cache.UserInfoCache;
import com.hs.mallchat.common.user.service.impl.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/20 - 9:14
 */
@Service
public class RoomAppServiceImpl implements RoomAppService {

    @Autowired
    private ContactDao contactDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private RoomFriendCache roomFriendCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ChatService chatService;
    @Autowired
    private IRoleService iRoleService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoomService roomService;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;

    /**
     * 获取会话列表--支持未登录态
     *
     * @param request
     * @param uid
     * @return
     */
    @Override
    public CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid) {
        // 查出用户要展示的会话列表
        CursorPageBaseResp<Long> page;
        if (Objects.nonNull(uid)) {
            Double hotEnd = getCursorOrNull(request.getCursor());
            Double hotStart = null;
            // 用户基础会话
            CursorPageBaseResp<Contact> contactPage = contactDao.getContactPage(uid, request);
            List<Long> baseRoomIds = contactPage.getList().stream()
                    .map(Contact::getRoomId)
                    .collect(Collectors.toList());
            if (!contactPage.getIsLast()) {
                hotStart = getCursorOrNull(contactPage.getCursor());
            }
            // 热门房间
            Set<ZSetOperations.TypedTuple<String>> typedTuples =
                    hotRoomCache.getRoomRange(hotStart, hotEnd);
            List<Long> hotRoomIds = typedTuples.stream()
                    .map(ZSetOperations.TypedTuple::getValue)
                    .filter(Objects::nonNull)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            baseRoomIds.addAll(hotRoomIds);
            // 基础会话和热门房间合并
            page = CursorPageBaseResp.init(contactPage, baseRoomIds);
        } else { // 用户未登录，只查全局房间
            CursorPageBaseResp<Pair<Long, Double>> roomCursorPage =
                    hotRoomCache.getRoomCursorPage(request);
            List<Long> roomIds = roomCursorPage.getList().stream()
                    .map(Pair::getKey)
                    .collect(Collectors.toList());
            page = CursorPageBaseResp.init(roomCursorPage, roomIds);
        }
        if (CollectionUtil.isEmpty(page.getList())) {
            return CursorPageBaseResp.empty();
        }
        // 最后组装会话信息（名称，头像，未读数等）
        List<ChatRoomResp> result = buildContactResp(uid, page.getList());
        return CursorPageBaseResp.init(page, result);
    }

    @Override
    public ChatRoomResp getContactDetail(Long uid, Long roomId) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        return buildContactResp(uid, Collections.singletonList(roomId)).get(0);
    }

    @Override
    public ChatRoomResp getContactDetailByFriend(Long uid, Long friendUid) {
        RoomFriend friendRoom = roomService.getFriendRoom(uid, friendUid);
        AssertUtil.isNotEmpty(friendRoom, "他不是您的好友");
        return buildContactResp(uid, Collections.singletonList(friendRoom.getRoomId())).get(0);
    }

    @Override
    @Cacheable(cacheNames = "member", key = "'memberList'+#request.roomId")
    public List<ChatMemberListResp> getMemberList(ChatMessageMemberReq request) {
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (isHotGroup(room)) { // 全员群展示所有用户100名
            List<User> memberList = userDao.getMemberList();
            return MemberAdapter.buildMemberList(memberList);
        } else {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            if (Objects.isNull(roomGroup)) {
                return MemberAdapter.buildMemberList(Collections.emptyList());
            }
            List<Long> memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
            if (CollectionUtil.isEmpty(memberUidList)) {
                return MemberAdapter.buildMemberList(Collections.emptyList());
            }
            Map<Long, User> batch = userInfoCache.getBatch(memberUidList);
            return MemberAdapter.buildMemberList(batch);
        }
    }

    /**
     * 删除群组成员
     * <p>
     * 本方法用于将指定的用户从群组中移除，包括以下步骤：
     * 1. 验证房间号是否正确；
     * 2. 验证操作者是否为群主或管理员；
     * 3. 判断被移除者是否能被移除，包括群主和管理员的特殊判断；
     * 4. 判断操作者是否有权限移除成员；
     * 5. 移除成员并发送事件通知；
     * 6. 清理缓存数据。
     * </p>
     *
     * @param uid     操作者的用户ID
     * @param request 包含要移除成员的信息和房间ID的请求对象
     * @throws Exception 如果操作不合法或数据不正确，将抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMember(Long uid, MemberDelReq request) {
        // 验证房间号是否存在
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");

        // 验证房间组是否存在
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, "房间号有误");

        // 验证操作者是否为群组成员
        GroupMember self = groupMemberDao.getMember(roomGroup.getId(), uid);
        AssertUtil.isNotEmpty(self, GroupErrorEnum.USER_NOT_IN_GROUP.getMsg());

        // 获取被移除用户的ID
        Long removedUid = request.getUid();

        // 判断被移除的人是否是群主，群主不可被移除
        AssertUtil.isFalse(groupMemberDao.isLord(roomGroup.getId(), removedUid), GroupErrorEnum.NOT_ALLOWED_FOR_REMOVE.getMsg());

        // 判断被移除的人是否是管理员，管理员只能被群主移除
        if (groupMemberDao.isManager(roomGroup.getId(), removedUid)) {
            boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
            AssertUtil.isTrue(isLord, GroupErrorEnum.NOT_ALLOWED_FOR_REMOVE.getMsg());
        }

        // 判断操作者是否有权限移除成员
        AssertUtil.isTrue(hasPower(self), GroupErrorEnum.NOT_ALLOWED_FOR_REMOVE.getMsg());

        // 验证被移除成员是否存在
        GroupMember member = groupMemberDao.getMember(roomGroup.getId(), removedUid);
        AssertUtil.isNotEmpty(member, "用户已经移除");

        // 移除群组成员
        groupMemberDao.removeById(member.getId());

        // 发送移除事件通知所有群成员
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), member.getUid());
        pushService.sendPushMsg(ws, memberUidList);

        // 清理缓存数据
        groupMemberCache.evictMemberUidList(room.getId());
    }

    /**
     * 邀请好友加入房间
     *
     * @param uid     当前操作的用户ID
     * @param request 包含要邀请的用户ID列表和房间ID的请求对象
     */
    @Override
    @RedissonLock(key = "#request.roomId") // 使用Redisson分布式锁，确保同一房间的成员操作是原子性的
    @Transactional(rollbackFor = Exception.class) // 事务注解，确保操作的原子性，遇到异常会回滚
    public void addMember(Long uid, MemberAddReq request) {
        Room room = roomCache.get(request.getRoomId()); // 从缓存中获取房间信息
        AssertUtil.isNotEmpty(room, "房间号有误"); // 验证房间是否存在

        // 判断是否为热门群组，热门群组不能邀请成员
        AssertUtil.isFalse(isHotGroup(room), "全员群无需邀请好友");

        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId()); // 从缓存中获取房间组信息
        AssertUtil.isNotEmpty(roomGroup, "房间号有误"); // 验证房间组是否存在

        GroupMember self = groupMemberDao.getMember(roomGroup.getId(), uid); // 查询当前操作用户是否为群成员
        AssertUtil.isNotEmpty(self, "您不是群成员"); // 验证当前操作用户是否为群成员

        List<Long> memberBatch = groupMemberDao.getMemberBatch(roomGroup.getId(), request.getUidList()); // 查询待邀请用户中已存在的群成员
        Set<Long> existUid = new HashSet<>(memberBatch); // 存储已存在的用户ID
        List<Long> waitAddUidList = request.getUidList().stream()
                .filter(a -> !existUid.contains(a)) // 过滤掉已存在的用户
                .distinct() // 去重
                .collect(Collectors.toList()); // 收集待添加的用户ID列表

        // 如果没有需要添加的用户，则直接返回
        if (CollectionUtils.isEmpty(waitAddUidList)) {
            return;
        }

        List<GroupMember> groupMembers = MemberAdapter.buildMemberAdd(roomGroup.getId(), waitAddUidList); // 构建群成员对象列表
        groupMemberDao.saveBatch(groupMembers); // 批量保存群成员

        // 发布事件，通知有新的群成员加入
        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, roomGroup, groupMembers, uid));
    }

    /**
     * 新增群组
     *
     * @param uid
     * @param request
     * @return
     */
    @Override
    @Transactional
    public Long addGroup(Long uid, GroupAddReq request) {
        RoomGroup roomGroup = roomService.createGroupRoom(uid);
        // 批量保存群成员
        List<GroupMember> groupMembers = RoomAdapter.buildGroupMemberBatch(request.getUidList(), roomGroup.getId());
        groupMemberDao.saveBatch(groupMembers);
        // 发送邀请加群消息==》触发每个人的会话
        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, roomGroup, groupMembers, uid));
        return roomGroup.getRoomId();
    }

    private boolean hasPower(GroupMember self) {
        return Objects.equals(self.getRole(), GroupRoleEnum.LEADER.getType())       // LEADER(1, "群主"),
                || Objects.equals(self.getRole(), GroupRoleEnum.MANAGER.getType())   // MANAGER(2, "管理"),MEMBER(3, "普通成员"),
                || iRoleService.hasPower(self.getUid(), RoleEnum.ADMIN);            // ADMIN(1L, "超级管理员"),CHAT_MANAGER(2L, "抹茶群聊管理员"),
    }

    /**
     * 获取群组信息
     *
     * @param uid
     * @param roomId
     */
    @Override
    public MemberResp getGroupDetail(Long uid, long roomId) {
        RoomGroup roomGroup = roomGroupCache.get(roomId);
        Room room = roomCache.get(roomId);
        // 也有可能不是群聊的会进来，需要对单聊进行拦截
        if(RoomTypeEnum.FRIEND.getType().equals(room.getType())){
            return MemberResp.builder().build();
        }
        AssertUtil.isNotEmpty(roomGroup, "roomId有误");
        Long onlineNum;
        if (isHotGroup(room)) {
            onlineNum = userCache.getOnlineNum();
        } else {
            List<Long> memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
            onlineNum = userDao.getOnlineCount(memberUidList).longValue();
        }
        GroupRoleAPPEnum groupRole = getGroupRole(uid, roomGroup, room);
        return MemberResp.builder()
                .avatar(roomGroup.getAvatar())
                .roomId(roomId)
                .groupName(roomGroup.getName())
                .onlineNum(onlineNum)
                .role(groupRole.getType())
                .build();
    }

    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request) {
        Room room = roomCache.get(request.getRoomId());
        AssertUtil.isNotEmpty(room, "房间号有误");
        List<Long> memberUidList;
        if (isHotGroup(room)) {  // 全员群展示所有用户
            memberUidList = null;
        } else { // 只展示房间内的群成员
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            if (Objects.isNull(roomGroup)) {
                return CursorPageBaseResp.empty();
            }
            memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
            if (CollectionUtil.isEmpty(memberUidList)) {
                return CursorPageBaseResp.empty();
            }
        }
        return chatService.getMemberPage(memberUidList, request);
    }

    private GroupRoleAPPEnum getGroupRole(Long uid, RoomGroup roomGroup, Room room) {
        GroupMember member = Objects.isNull(uid) ? null : groupMemberDao.getMember(roomGroup.getId(), uid);
        if (Objects.nonNull(member)) {
            return GroupRoleAPPEnum.of(member.getRole());
        } else if (isHotGroup(room)) {
            return GroupRoleAPPEnum.MEMBER;
        } else {
            return GroupRoleAPPEnum.REMOVE;
        }
    }

    private boolean isHotGroup(Room room) {
        return HotFlagEnum.YES.getType().equals(room.getHotFlag());
    }

    private Double getCursorOrNull(String cursor) {
        return Optional.ofNullable(cursor)
                .map(Double::parseDouble)
                .orElse(null);
    }

    @NotNull
    private List<ChatRoomResp> buildContactResp(Long uid, List<Long> roomIds) {
        // 表情和头像
        Map<Long, RoomBaseInfo> roomBaseInfoMap = getRoomBaseInfoMap(roomIds, uid);
        // 最后一条消息
        List<Long> msgIds = roomBaseInfoMap.values().stream()
                .map(RoomBaseInfo::getLastMsgId)
                .collect(Collectors.toList());
        List<Message> messages = CollectionUtil.isEmpty(msgIds) ? new ArrayList<>() : messageDao.listByIds(msgIds);
        Map<Long, Message> msgMap = messages.stream().collect(Collectors.toMap(Message::getId, Function.identity()));
        Map<Long, User> lastMsgUidMap = userInfoCache.getBatch(messages.stream().map(Message::getFromUid).collect(Collectors.toList()));
        // 消息未读数
        Map<Long, Integer> unReadCountMap = getUnReadCountMap(uid, roomIds);
        return roomBaseInfoMap.values().stream().map(room -> {
                    ChatRoomResp resp = new ChatRoomResp();
                    RoomBaseInfo roomBaseInfo = roomBaseInfoMap.get(room.getRoomId());
                    resp.setAvatar(roomBaseInfo.getAvatar());
                    resp.setRoomId(room.getRoomId());
                    resp.setActiveTime(room.getActiveTime());
                    resp.setHot_Flag(roomBaseInfo.getHotFlag());
                    resp.setType(roomBaseInfo.getType());
                    resp.setName(roomBaseInfo.getName());
                    Message message = msgMap.get(room.getLastMsgId());
                    if (Objects.nonNull(message)) {
                        AbstractMsgHandler strategyNoNull = MsgHandlerFactory.getStrategyNoNull(message.getType());
                        resp.setText(lastMsgUidMap.get(message.getFromUid()).getName() + ":" + strategyNoNull.showContactMsg(message));
                    }
                    resp.setUnreadCount(unReadCountMap.getOrDefault(room.getRoomId(), 0));
                    return resp;
                }).sorted(Comparator.comparing(ChatRoomResp::getActiveTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取未读数
     */
    private Map<Long, Integer> getUnReadCountMap(Long uid, List<Long> roomIds) {
        if (Objects.isNull(uid)) {
            return new HashMap<>();
        }
        List<Contact> contacts = contactDao.getByRoomIds(roomIds, uid);
        return contacts.parallelStream()
                .map(contact -> Pair.of(contact.getRoomId(),
                        messageDao.getUnReadCount(contact.getRoomId(), contact.getReadTime())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Map<Long, RoomBaseInfo> getRoomBaseInfoMap(List<Long> roomIds, Long uid) {
        Map<Long, Room> roomMap = roomCache.getBatch(roomIds); // 进入string redis批量缓存batch框架
        // 房间根据好友和群组类型分组
        Map<Integer, List<Long>> groupRoomIdMap = roomMap.values().stream()
                .collect(Collectors.groupingBy(Room::getType,
                        Collectors.mapping(Room::getId, Collectors.toList())));
        // 获取群组信息
        List<Long> groupRoomId = groupRoomIdMap.get(RoomTypeEnum.GROUP.getType());
        Map<Long, RoomGroup> roomInfoBatch = roomGroupCache.getBatch(groupRoomId);
        // 获取好友信息
        List<Long> friendRoomId = groupRoomIdMap.get(RoomTypeEnum.FRIEND.getType());
        Map<Long, User> friendRoomMap = getFriendRoomMap(friendRoomId, uid);

        return roomMap.values().stream().map(room -> {
            RoomBaseInfo roomBaseInfo = new RoomBaseInfo();
            roomBaseInfo.setRoomId(room.getId());
            roomBaseInfo.setType(room.getType());
            roomBaseInfo.setHotFlag(room.getHotFlag());
            roomBaseInfo.setLastMsgId(room.getLastMsgId());
            roomBaseInfo.setActiveTime(room.getActiveTime());
            if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.GROUP) {
                RoomGroup roomGroup = roomInfoBatch.get(room.getId());
                roomBaseInfo.setName(roomGroup.getName());
                roomBaseInfo.setAvatar(roomGroup.getAvatar());
            } else if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.FRIEND) {
                User user = friendRoomMap.get(room.getId());
                roomBaseInfo.setName(user.getName());
                roomBaseInfo.setAvatar(user.getAvatar());
            }
            return roomBaseInfo;
        }).collect(Collectors.toMap(RoomBaseInfo::getRoomId, Function.identity()));
    }

    private Map<Long, User> getFriendRoomMap(List<Long> roomIds, Long uid) {
        if (CollectionUtil.isEmpty(roomIds)) {
            return new HashMap<>();
        }
        Map<Long, RoomFriend> roomFriendMap = roomFriendCache.getBatch(roomIds);
        Set<Long> friendUidSet = ChatAdapter.getFriendUidSet(roomFriendMap.values(), uid);
        Map<Long, User> userBatch = userInfoCache.getBatch(new ArrayList<>(friendUidSet));
        return roomFriendMap.values().stream()
                .collect(Collectors.toMap(RoomFriend::getRoomId, roomFriend -> {
                    Long friendUid = ChatAdapter.getFriendUid(roomFriend, uid);
                    return userBatch.get(friendUid);
                }));
    }
}
