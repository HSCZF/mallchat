package com.hs.mallchat.common.user.service.adapter;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;

/**
 * @Author: CZF
 * @Create: 2024/5/29 - 10:24
 */
public class TextBuilder {

    public static WxMpXmlOutMessage build(String content, WxMpXmlMessage wxMpXmlMessage) {
        WxMpXmlOutTextMessage wxMsg = WxMpXmlOutMessage.TEXT().content(content)
                .fromUser(wxMpXmlMessage.getToUser())
                .toUser(wxMpXmlMessage.getFromUser())
                .build();
        return wxMsg;
    }
}
