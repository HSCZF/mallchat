package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;

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
}
