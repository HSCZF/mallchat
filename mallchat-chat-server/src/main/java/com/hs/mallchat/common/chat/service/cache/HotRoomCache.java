package com.hs.mallchat.common.chat.service.cache;

import com.hs.mallchat.common.common.constant.RedisKey;
import com.hs.mallchat.common.common.utils.RedisUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Description: 全局房间
 *
 * @Author: CZF
 * @Create: 2024/7/29 - 15:49
 */
@Component
public class HotRoomCache {

    /**
     * 更新热门群聊的最新时间
     * @param roomId 热门群聊Id
     * @param refreshTime 刷新最新时间
     */
    public void refreshActiveTime(Long roomId, Date refreshTime) {
        RedisUtils.zAdd(RedisKey.getKey(RedisKey.HOT_ROOM_ZET), roomId, (double) refreshTime.getTime());
    }

}
