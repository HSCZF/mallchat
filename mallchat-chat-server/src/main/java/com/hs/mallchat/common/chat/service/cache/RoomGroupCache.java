package com.hs.mallchat.common.chat.service.cache;

import com.hs.mallchat.common.chat.dao.RoomGroupDao;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;
import com.hs.mallchat.common.common.constant.RedisKey;
import com.hs.mallchat.common.common.service.cache.AbstractRedisStringCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description: 群组基本信息的缓存
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 20:42
 */
@Component
public class RoomGroupCache extends AbstractRedisStringCache<Long, RoomGroup> {

    @Autowired
    private RoomGroupDao roomGroupDao;
    ;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.GROUP_INFO_STRING, roomId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, RoomGroup> load(List<Long> roomIds) {
        List<RoomGroup> roomGroups = roomGroupDao.listByRoomIds(roomIds);
        // bug修复
        // 在debug到AbstractRedisStringCache的load = load(loadReqs)的时候，比如表room_group有个房间id是6，roomId是8，全员群是手动数据库添加的，所以起初不会到这里面
        // 拼接的时候key接上的是6不是8，导致会话端口获取不到数据
        // 正确的是RoomGroup::getRoomId
        return roomGroups.stream().collect(Collectors.toMap(RoomGroup::getRoomId, Function.identity()));
    }

}
