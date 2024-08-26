package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageMemberReq;
import com.hs.mallchat.common.chat.domain.vo.request.GroupAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.MemberDelReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMemberListResp;
import com.hs.mallchat.common.chat.domain.vo.response.ChatRoomResp;
import com.hs.mallchat.common.chat.domain.vo.response.MemberResp;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.user.domain.vo.response.ws.ChatMemberResp;

import java.util.List;

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

    CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request);

    List<ChatMemberListResp> getMemberList(ChatMessageMemberReq request);

    void delMember(Long uid, MemberDelReq request);

    Long addGroup(Long uid, GroupAddReq request);

    void addMember(Long uid, MemberAddReq request);

    /**
     * 获取群组信息
     */
    MemberResp getGroupDetail(Long uid, long roomId);
}
