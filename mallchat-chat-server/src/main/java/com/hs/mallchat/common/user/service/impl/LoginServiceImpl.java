package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.constant.RedisKey;
import com.hs.mallchat.common.common.utils.JwtUtils;
import com.hs.mallchat.common.common.utils.RedisUtils;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 12:04
 */
@Service
public class LoginServiceImpl implements LoginService {

    public static final int TOKEN_EXPIRE_DAYS = 3;

    public static final int TOKEN_RENEWAL_DAYS = 1;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 异步检查并刷新令牌的有效期。
     * 如果令牌的有效期小于预设的刷新阈值，则延长令牌的有效期。
     * 这种机制旨在避免频繁的令牌刷新操作，只在令牌即将过期时进行刷新。
     *
     * @param token 待检查的令牌字符串。
     * @Async 异步执行，在ThreadPoolConfig统一配置
     */
    @Override
    @Async
    public void renewalTokenIfNecessary(String token) {
        // 根据令牌获取对应用户的ID。
        Long validUid = getValidUid(token);
        // 根据用户ID生成Redis中的令牌键。
        String userTokenKey = getUserTokenKey(validUid);
        // 获取令牌的剩余有效期，以天为单位。
        Long expireDays = RedisUtils.getExpire(userTokenKey, TimeUnit.DAYS);
        // 如果令牌不存在，则不进行任何操作。
        if (expireDays == -2) { //不存在的key
            return;
        }
        // 如果令牌的有效期小于预设的刷新阈值，则延长令牌的有效期。
        if (expireDays < TOKEN_RENEWAL_DAYS) {
            RedisUtils.expire(getUserTokenKey(validUid), TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        }
    }



    /**
     * 登录成功，获取token
     *
     * @param uid
     * @return 返回token
     */
    @Override
    public String login(Long uid) {
        String token = jwtUtils.createToken(uid);
        RedisUtils.set(getUserTokenKey(uid), token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        return token;
    }

    /**
     * 如果token有效，返回uid
     *
     * @param token
     * @return
     */
    @Override
    public Long getValidUid(String token) {
        Long uid = jwtUtils.getUidOrNull(token);
        if (Objects.isNull(uid)) {
            return null;
        }
        String oldToken = RedisUtils.get(getUserTokenKey(uid));
        return Objects.equals(oldToken, token) ? uid : null;
    }

    private String getUserTokenKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_TOKEN_STRING, uid);
    }
}
