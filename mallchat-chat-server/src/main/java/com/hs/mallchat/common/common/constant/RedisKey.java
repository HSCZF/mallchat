package com.hs.mallchat.common.common.constant;

import java.util.Objects;

/**
 * @Author: CZF
 * @Create: 2024/6/3 - 11:22
 */
public class RedisKey {
    private static final String BASE_KEY = "mallchat:";

    /**
     * 用户token的key
     */
    public static final String USER_TOKEN_STRING = "userToken:uid_%d";
    /**
     * 用户信息
     */
    public static final String USER_INFO_STRING = "userInfo:uid_%d";
    /**
     * 用户的信息更新时间
     */
    public static final String USER_MODIFY_STRING = "userModify:uid_%d";

    /**
     * 用户的信息汇总
     */
    public static final String USER_SUMMARY_STRING = "userSummary:uid_%d";

    /**
     * 房间 详情
     */
    public static final String ROOM_INFO_STRING = "roomInfo:roomId_%d";

    /**
     * 群组详情
     */
    public static final String GROUP_INFO_STRING = "groupInfo:roomId_%d";


    public static String getKey(String key, Object... objects) {
        return BASE_KEY + String.format(key, objects);
    }

}
