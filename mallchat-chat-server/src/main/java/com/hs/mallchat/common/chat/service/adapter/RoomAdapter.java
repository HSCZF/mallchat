package com.hs.mallchat.common.chat.service.adapter;

import com.hs.mallchat.common.chat.domain.entity.Contact;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageReadResp;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * 消息适配器
 *
 * @Author: CZF
 * @Create: 2024/8/21 - 9:07
 */
public class RoomAdapter {
    public static List<ChatMessageReadResp> buildReadResp(List<Contact> list) {
        return list.stream().map(contact -> {
            ChatMessageReadResp resp = new ChatMessageReadResp();
            resp.setUid(contact.getUid());
            return resp;
        }).collect(Collectors.toList());
    }
}
