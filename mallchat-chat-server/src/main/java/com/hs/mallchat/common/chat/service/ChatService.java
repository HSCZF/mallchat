package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessagePageReq;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMemberStatisticResp;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageResp;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;

import javax.annotation.Nullable;


/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 20:17
 */
public interface ChatService {

    /**
     * 发送消息
     * @param request
     * @param uid
     * @return
     */
    Long sendMsg(ChatMessageReq request, Long uid);

    /**
     * 根据消息获取消息前端展示的物料
     *
     * @param message
     * @param receiveUid 接受消息的uid，可null
     * @return
     */
    ChatMessageResp getMsgResp(Message message, Long receiveUid);

    /**
     * 根据消息获取消息前端展示的物料
     *
     * @param msgId
     * @param receiveUid 接受消息的uid，可null
     * @return
     */
    ChatMessageResp getMsgResp(Long msgId, Long receiveUid);

    ChatMemberStatisticResp getMemberStatistic();

    /**
     * 获取消息列表
     *
     * @param request
     * @return
     */
    CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq request, @Nullable Long receiveUid);
}
