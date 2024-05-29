package com.hs.mallchat.common.user.service.handler;

import com.hs.mallchat.common.user.service.WXMsgService;
import com.hs.mallchat.common.user.service.adapter.TextBuilder;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author czf
 * 订阅
 */
@Component
public class SubscribeHandler extends AbstractHandler {


    @Autowired
    private WXMsgService wxMsgService;
    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) throws WxErrorException {

        this.logger.info("新关注用户 OPENID: " + wxMpXmlMessage.getFromUser());
        // 事件码 qrscene_2
        WxMpXmlOutMessage responseResult = null;
        try {
             responseResult = wxMsgService.scan(wxMpXmlMessage);
        }catch (Exception e){
            this.logger.error(e.getMessage(), e);
        }
        if(responseResult != null){
            return responseResult;
        }

        return TextBuilder.build("你好，感谢关注！", wxMpXmlMessage);
    }


}
