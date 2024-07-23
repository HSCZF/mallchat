package com.hs.mallchat.common.chat.service.cache;

import com.hs.mallchat.common.chat.dao.RoomDao;
import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.common.constant.RedisKey;
import com.hs.mallchat.common.common.service.cache.AbstractRedisStringCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 20:31
 */
@Component
public class RoomCache extends AbstractRedisStringCache<Long, Room> {

    @Autowired
    private RoomDao roomDao;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.ROOM_INFO_STRING, roomId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, Room> load(List<Long> roomIds) {
        List<Room> rooms = roomDao.listByIds(roomIds);
        return rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
    }
}
