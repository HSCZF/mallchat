package com.hs.mallchat.common.websocket.service.adapter;

import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.hs.mallchat.common.websocket.domain.vo.response.*;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

/**
 * @Author: CZF
 * @Create: 2024/5/29 - 17:18
 */
public class WebSocketAdapter {


    /**
     * LOGIN_URL(1, "登录二维码返回", WSLoginUrl.class),
     * @param wxMpQrCodeTicket
     * @return
     */
    public static WSBaseResp<?> buildLoginResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
        WSBaseResp<WSLoginUrl> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        resp.setData(new WSLoginUrl(wxMpQrCodeTicket.getUrl()));
        return resp;
    }

    /**
     * LOGIN_SCAN_SUCCESS(2, "用户扫描成功等待授权", null),
     * @return
     */
    public static WSBaseResp<?> buildScanSuccessResp() {
        WSBaseResp<WSLoginUrl> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SCAN_SUCCESS.getType());
        return resp;
    }

    /**
     * LOGIN_SUCCESS(3, "用户登录成功返回用户信息", WSLoginSuccess.class),
     * @param user
     * @param code
     * @return
     */
    public static WSBaseResp<?> buildLoginSuccessResp(User user, String code) {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        WSLoginSuccess build = WSLoginSuccess.builder()
                .avatar(user.getAvatar())
                .name(user.getName())
                .token(code)
                .uid(user.getId())
                .build();
        resp.setData(build);
        return resp;
    }


}
