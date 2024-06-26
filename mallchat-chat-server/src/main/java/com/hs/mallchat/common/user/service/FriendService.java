package com.hs.mallchat.common.user.service;

import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendCheckReq;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendCheckResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendResp;

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

    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request);
}
