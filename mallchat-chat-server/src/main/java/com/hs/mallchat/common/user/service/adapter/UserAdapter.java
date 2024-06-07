package com.hs.mallchat.common.user.service.adapter;

import com.hs.mallchat.common.common.domain.dto.RequestInfo;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.vo.response.UserInfoResp;
import jodd.bean.BeanUtil;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.springframework.beans.BeanUtils;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 10:34
 */
public class UserAdapter {


    /**
     * 根据openid构建用户对象，用于保存用户信息。
     *
     * @param openId 用户的openid，用于唯一标识用户。
     * @return User对象，包含openid信息。
     */
    public static User buildUserSave(String openId) {
        return User.builder().openId(openId).build();
    }

    /**
     * 根据用户ID和微信OAuth2用户信息构建授权用户对象。
     *
     * @param uid 用户的唯一标识ID。
     * @param userInfo 微信OAuth2提供的用户信息。
     * @return User对象，包含用户的基本信息。
     */
    public static User buildAuthorizeUser(Long uid, WxOAuth2UserInfo userInfo) {
        User user = new User();
        user.setId(uid);
        user.setName(userInfo.getNickname());
        user.setAvatar(userInfo.getHeadImgUrl());
        return user;
    }

    /**
     * 根据用户对象和修改名称的次数构建用户信息响应对象。
     *
     * @param user 用户对象，包含用户的基本信息。
     * @param modifyNameCount 用户剩余的修改名称的次数。
     * @return UserInfoResp对象，包含用户信息和修改名称的次数。
     */
    public static UserInfoResp buildUserInfo(User user, Integer modifyNameCount) {
        UserInfoResp vo = new UserInfoResp();
        BeanUtils.copyProperties(user, vo);
        vo.setId(user.getId());
        vo.setModifyNameChance(modifyNameCount);
        return vo;
    }
}
