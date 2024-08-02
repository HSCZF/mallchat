package com.hs.mallchat.common.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.hs.mallchat.common.chat.domain.entity.RoomFriend;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.RoomService;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.request.PageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.domain.vo.response.PageBaseResp;
import com.hs.mallchat.common.common.event.UserApplyEvent;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.dao.UserApplyDao;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.dao.UserFriendDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.entity.UserApply;
import com.hs.mallchat.common.user.domain.entity.UserFriend;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApplyReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApproveReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendCheckReq;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendApplyResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendCheckResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendUnreadResp;
import com.hs.mallchat.common.user.service.FriendService;
import com.hs.mallchat.common.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hs.mallchat.common.user.domain.enums.ApplyStatusEnum.WAIT_APPROVAL;

/**
 * @Author: CZF
 * @Create: 2024/6/24 - 9:45
 * Description:
 */
@Service
@Slf4j
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserFriendDao userFriendDao;
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ChatService chatService;

    /**
     * 检查是否是自己好友
     * <p>
     * 该方法通过查询数据库来确定指定的UIDs是否为当前用户的朋友。它首先根据当前用户和指定的UID列表查询用户朋友表，
     * 然后将查询结果与请求的UID列表进行比较，以确定每个UID是否是朋友。
     *
     * @param uid     当前用户的UID。
     * @param request 包含待检查的UID列表的请求对象。
     * @return 包含检查结果的响应对象，其中每个UID对应一个检查结果。
     */
    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq request) {
        // 根据当前用户UID和请求的UID列表查询用户朋友表
        List<UserFriend> friendList = userFriendDao.getByFriends(uid, request.getUidList());
        // 将查询到的朋友UID转换为Set集合，方便后续的查找操作
        Set<Long> friendUidSet = friendList.stream()
                .map(UserFriend::getFriendUid)
                .collect(Collectors.toSet());

        // 遍历请求的UID列表，对每个UID生成一个检查结果对象，并设置其是否为朋友的标志
        List<FriendCheckResp.FriendCheck> friendCheckList
                = request.getUidList().stream()
                .map(friendUid -> {
                    FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
                    friendCheck.setUid(friendUid);
                    friendCheck.setIsFriend(friendUidSet.contains(friendUid));
                    return friendCheck;
                }).collect(Collectors.toList());

        // 创建并返回包含所有检查结果的响应对象
        return new FriendCheckResp(friendCheckList);
    }

    /**
     * 申请好友
     *
     * @param uid     uid
     * @param request 请求
     */
    @Override
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq request) {
        //是否有好友关系
        UserFriend friend = userFriendDao.getByFriend(uid, request.getTargetUid());
        AssertUtil.isEmpty(friend, "你们已经是好友了");
        //是否有待审批的申请记录(自己的)
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, request.getTargetUid());
        if (Objects.nonNull(selfApproving)) {
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, request.getTargetUid());
            return;
        }
        //是否有待审批的申请记录(别人请求自己的)
        UserApply friendApproving = userApplyDao.getFriendApproving(request.getTargetUid(), uid);
        if (Objects.nonNull(friendApproving)) {
            ((FriendService) AopContext.currentProxy()).applyApprove(uid, new FriendApproveReq(friendApproving.getId()));
            return;
        }
        //申请入库
        UserApply insert = FriendAdapter.buildFriendApply(uid, request);
        userApplyDao.save(insert);
        //申请事件
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, insert));
    }

    /**
     * 同意好友申请
     *
     * @param uid     uid
     * @param request 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq request) {
        UserApply userApply = userApplyDao.getById(request.getApplyId());
        AssertUtil.isNotEmpty(userApply, "不存在申请记录");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), WAIT_APPROVAL.getCode(), "已同意好友申请");
        //同意申请
        userApplyDao.agree(request.getApplyId());
        //创建双方好友关系
        createFriend(uid, userApply.getUid());
        //创建一个聊天房间
        RoomFriend roomFriend = roomService.createFriendRoom(Arrays.asList(uid, userApply.getUid()));
        //发送一条同意消息。。我们已经是好友了，开始聊天吧
        chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()), uid);
    }

    /**
     * 删除好友
     *
     * @param uid       uid
     * @param friendUid 朋友uid
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long uid, Long friendUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, friendUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{}，{}", uid, friendUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream()
                .map(UserFriend::getId)
                .collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        // todo 禁用房间

    }

    private void createFriend(Long uid, Long targetUid) {
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUid(uid);
        userFriend1.setFriendUid(targetUid);
        UserFriend userFriend2 = new UserFriend();
        userFriend2.setUid(targetUid);
        userFriend2.setFriendUid(uid);
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1, userFriend2));
    }

    /**
     * 申请未读数
     *
     * @param uid
     * @return {@link FriendUnreadResp}
     */
    @Override
    public FriendUnreadResp unread(Long uid) {
        Integer unReadCount = userApplyDao.getUnReadCount(uid);
        return new FriendUnreadResp(unReadCount);
    }

    /**
     * 分页查询好友申请
     *
     * @param uid     用户ID，用于查询针对该用户的好友申请
     * @param request 分页请求对象，包含分页信息
     * @return 返回分页响应对象，其中包含好友申请的详细信息
     * <p>
     * 此方法首先根据用户ID和分页请求信息查询好友申请数据。
     * 如果查询结果为空，则返回一个空的分页响应对象。
     * 否则，将查询到的好友申请标记为已读，并构造相应的分页响应对象返回。
     */
    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request) {
        // 根据用户ID和分页请求信息查询好友申请数据
        IPage<UserApply> userApplyPage = userApplyDao.friendApplyPage(uid, request.plusPage());

        // 如果查询结果为空，则返回空的分页响应对象
        if (CollectionUtils.isEmpty(userApplyPage.getRecords())) {
            return PageBaseResp.empty();
        }

        // 将查询到的好友申请标记为已读
        // 将这些申请列表设为已读
        readApples(uid, userApplyPage);

        // 构造分页响应对象并返回，其中包含处理后的好友申请列表
        // 返回消息
        return PageBaseResp.init(userApplyPage, FriendAdapter.buildFriendApplyList(userApplyPage.getRecords()));
    }


    private void readApples(Long uid, IPage<UserApply> userApplyPage) {

        List<Long> applyIds = userApplyPage.getRecords()
                .stream()
                .map(UserApply::getId)
                .collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request) {
        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, request);
        if (CollectionUtils.isEmpty(friendPage.getList())) {
            return CursorPageBaseResp.empty();
        }
        List<Long> friendUids = friendPage.getList()
                .stream().map(UserFriend::getFriendUid)
                .collect(Collectors.toList());
        List<User> userList = userDao.getFriendList(friendUids);
        return CursorPageBaseResp.init(friendPage, FriendAdapter.buildFriend(friendPage.getList(), userList));
    }
}
