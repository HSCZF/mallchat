package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.vo.response.ChatRoomResp;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/20 - 9:14
 */
public interface RoomAppService {
    /**
     * 获取会话列表--支持未登录态
     * @param request
     * @param uid
     * @return
     */
    CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid);

    ChatRoomResp getContactDetail(Long uid, Long roomId);

    ChatRoomResp getContactDetailByFriend(Long uid, Long friendUid);
}
