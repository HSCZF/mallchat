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

    public WxMpXmlOutMessage build(String content, WxMpXmlMessage wxMessage, WxMpService service) {
        WxMpXmlOutTextMessage wxMsg = WxMpXmlOutMessage.TEXT().content(content)
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .build();
        return wxMsg;
    }
}
