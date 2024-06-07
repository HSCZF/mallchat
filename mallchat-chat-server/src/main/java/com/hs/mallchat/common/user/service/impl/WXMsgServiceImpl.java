package com.hs.mallchat.common.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.service.UserService;
import com.hs.mallchat.common.user.service.WXMsgService;
import com.hs.mallchat.common.user.service.adapter.TextBuilder;
import com.hs.mallchat.common.user.service.adapter.UserAdapter;
import com.hs.mallchat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.velocity.runtime.directive.Foreach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 9:39
 */
@Service
@Slf4j
public class WXMsgServiceImpl implements WXMsgService {

    /**
     * 用户同意授权，获取code,引导关注者打开如下页面
     * https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
     * 需要更改一些东西，使用%s占位符替换，注意是小写的s
     * 在把scope替换成scope=snsapi_userinfo
     */
    public static final String URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";

    /**
     * openid和登录code的关系map，这里也有可能oom，一直保存不授权，后期加入集群，这里可以不用管
     */
    private static final ConcurrentHashMap<String, Integer> WAIT_AUTHORIZE_MAP = new ConcurrentHashMap<>();

    @Value("${wx.mp.callback}")
    private String callback;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserService userService;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * 打破循环依赖：wxMpConfiguration依赖subscribeHandler，而subscribeHandler依赖WXMsgServiceImpl，所以使用@lazy
     *
     * @Lazy
     */
    @Autowired
    @Lazy
    private WxMpService wxMpService;

    @Override
    public WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage) {
        String openId = wxMpXmlMessage.getFromUser();
        Integer code = getEventKey(wxMpXmlMessage);
        if (Objects.isNull(code)) {
            return null;
        }
        User user = userDao.getByOpenId(openId);
        boolean registered = Objects.nonNull(user);
        boolean authorized = registered && StrUtil.isNotBlank(user.getAvatar());
        // 用户注册并授权
        if (registered && authorized) {
            // 登录成功的逻辑，通过code找到给前端channel推送消息
            webSocketService.scanLoginSuccess(code, user.getId());
            return null;
        }
        // 用户未注册，先注册
        if (!registered) {
            User insert = UserAdapter.buildUserSave(openId);
            userService.registered(insert);
        }
        // 推送链接让用户授权
        WAIT_AUTHORIZE_MAP.put(openId, code);
        webSocketService.scanSuccess(code);
        String authorizeUrl = String.format(URL,
                wxMpService.getWxMpConfigStorage().getAppId(),
                URLEncoder.encode(callback + "/wx/portal/public/callBack")
        );
        System.out.println("登录链接：" + authorizeUrl);
        return TextBuilder.build("请点击登录：<a href=\"" + authorizeUrl + "\">登录</a>", wxMpXmlMessage);
    }

    /**
     * 用户授权
     *
     * @param userInfo
     */
    @Override
    public void authorize(WxOAuth2UserInfo userInfo) {
        String openId = userInfo.getOpenid();
        User user = userDao.getByOpenId(openId);
        // 更新用户信息
        if (StrUtil.isBlank(user.getAvatar())) {
            fillUserInfo(user.getId(), userInfo);
        }
        // 通过code找到给用户channel进行登录
        Integer code = WAIT_AUTHORIZE_MAP.remove(openId);
        // 这个是大坑啊，没写下面这个webSocket的登录回调，拿不到type:3的数据
        // 登录了一次，再次点击登录就会空指针错误，因为已经登录成功了，临时保存的WAIT_AUTHORIZE_MAP的code已经移除了
        // 后续在对这个登录链接进行处理，要测验一直登录的话，那换成这个Integer code = WAIT_AUTHORIZE_MAP.get(openId);
        // 后续使用RedisUtils工具类获取
        webSocketService.scanLoginSuccess(code,user.getId());
    }

    private void fillUserInfo(Long uid, WxOAuth2UserInfo userInfo) {
        User user = UserAdapter.buildAuthorizeUser(uid, userInfo);
        userDao.updateById(user);
    }

    private Integer getEventKey(WxMpXmlMessage wxMpXmlMessage) {
        try {
            String eventKey = wxMpXmlMessage.getEventKey();
            String code = eventKey.replace("qrscene_", "");
            return Integer.parseInt(code);
        } catch (Exception e) {
            log.error("getEventKey error eventkey:{}", wxMpXmlMessage.getEventKey(), e);
            return null;
        }

    }
}
