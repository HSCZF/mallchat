package com.hs.mallchat.common.chat.service.cache;

import com.hs.mallchat.common.chat.dao.GroupMemberDao;
import com.hs.mallchat.common.chat.dao.RoomGroupDao;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Description: 群成员相关缓存
 *
 * @Author: CZF
 * @Create: 2024/7/29 - 16:21
 */
@Component
public class GroupMemberCache {

    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private GroupMemberDao groupMemberDao;

    @Cacheable(cacheNames = "member",key = "'groupMember'+#roomId")
    public List<Long> getMemberUidList(Long roomId) {
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        if (Objects.isNull(roomGroup)) {
            return null;
        }
        return groupMemberDao.getMemberUidList(roomGroup.getId());
    }

    @CacheEvict(cacheNames = "member", key = "'groupMember'+#roomId")
    public List<Long> evictMemberUidList(Long roomId) {
        return null;
    }

}
