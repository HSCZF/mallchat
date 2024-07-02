package com.hs.mallchat.common.user.service;

import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.request.PageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.domain.vo.response.PageBaseResp;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApplyReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApproveReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendCheckReq;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendApplyResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendCheckResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendUnreadResp;

/**
 * @Author: CZF
 * @Create: 2024/6/24 - 9:45
 * Description: 好友
 */
public interface FriendService {

    /**
     * 检查
     * 检查是否是自己好友
     *
     * @param request 请求
     * @param uid     uid
     * @return {@link FriendCheckResp}
     */
    FriendCheckResp check(Long uid, FriendCheckReq request);

    /**
     * 联系人列表
     * @param uid       uid
     * @param request   请求
     * @return {@link CursorPageBaseResp}<{@link FriendResp}>
     */
    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request);

    /**
     * 分页查询好友申请
     *
     * @param request 请求
     * @return {@link PageBaseResp}<{@link FriendApplyResp}>
     */
    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request);

    /**
     * 申请未读数
     *
     * @return {@link FriendUnreadResp}
     */
    FriendUnreadResp unread(Long uid);

    /**
     * 申请好友
     *
     * @param request 请求
     * @param uid     uid
     */
    void apply(Long uid, FriendApplyReq request);

    /**
     * 同意好友申请
     *
     * @param uid     uid
     * @param request 请求
     */
    void applyApprove(Long uid, FriendApproveReq request);

    /**
     * 删除好友
     *
     * @param uid       uid
     * @param friendUid 朋友uid
     */
    void deleteFriend(Long uid, Long friendUid);
}
