package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.user.dao.UserApplyDao;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.dao.UserFriendDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.entity.UserFriend;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendCheckReq;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendCheckResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendResp;
import com.hs.mallchat.common.user.service.FriendService;
import com.hs.mallchat.common.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * 检查是否是自己好友
     * <p>
     * 该方法通过查询数据库来确定指定的UIDs是否为当前用户的朋友。它首先根据当前用户和指定的UID列表查询用户朋友表，
     * 然后将查询结果与请求的UID列表进行比较，以确定每个UID是否是朋友。
     *
     * @param uid 当前用户的UID。
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
