package com.hs.mallchat.common.common.constant;

import java.util.Objects;

/**
 * @Author: CZF
 * @Create: 2024/6/3 - 11:22
 */
public class RedisKey {
    private static final String BASE_KEY = "mallchat:chat";

    /**
     * 用户token的key
     */
    public static final String USER_TOKEN_STRING = "userToken:uid_%d";

    public static String getKey(String key, Object... o) {
        return BASE_KEY + String.format(key, o);
    }

}
