package com.hs.mallchat.common.user.service;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 12:04
 */
public interface LoginService {


    /**
     * 刷新token有效期
     *
     * @param token
     */
    void renewalTokenIfNecessary(String token);

    /**
     * 登录成功，获取token
     *
     * @param uid
     * @return 返回token
     */
    String login(Long uid);

    /**
     * 如果token有效，返回uid
     *
     * @param token
     * @return
     */
    Long getValidUid(String token);

    /**
     * 校验token是不是有效
     *
     * @param token
     * @return
     */
    boolean verify(String token);
}
