package com.hs.mallchat.common.user.service.cache;

import cn.hutool.core.lang.Pair;
import com.hs.mallchat.common.common.constant.RedisKey;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.utils.RedisUtils;
import com.hs.mallchat.common.user.dao.BlackDao;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.dao.UserRoleDao;
import com.hs.mallchat.common.user.domain.entity.Black;
import com.hs.mallchat.common.user.domain.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: CZF
 * @Create: 2024/6/18 - 11:11
 * Description: 用户相关缓存
 */
@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private BlackDao blackDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserSummaryCache userSummaryCache;

    public Long getOnlineNum() {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        return RedisUtils.zCard(onlineKey);
    }

    //移除用户
    public void remove(Long uid) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        String offlineKey = RedisKey.getKey(RedisKey.OFFLINE_UID_ZET);
        //移除离线表
        RedisUtils.zRemove(offlineKey, uid);
        //移除上线表
        RedisUtils.zRemove(onlineKey, uid);
    }

    //用户上线
    public void online(Long uid, Date optTime) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        String offlineKey = RedisKey.getKey(RedisKey.OFFLINE_UID_ZET);
        //移除离线表
        RedisUtils.zRemove(offlineKey, uid);
        //更新上线表
        RedisUtils.zAdd(onlineKey, uid, optTime.getTime());
    }

    /**
     * 用户下线
     *
     * @param uid
     * @param optTime
     */
    public void offline(Long uid, Date optTime) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        String offlineKey = RedisKey.getKey(RedisKey.OFFLINE_UID_ZET);
        // 移除上线列表
        RedisUtils.zRemove(onlineKey, uid);
        // 更新上线表
        RedisUtils.zAdd(offlineKey, uid, optTime.getTime());

    }

    public boolean isOnline(Long uid) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        return RedisUtils.zIsMember(onlineKey, uid);
    }


    public List<Long> getUserModifyTime(List<Long> uidList) {
        System.out.println("UserCache.getUserModifyTime");
        List<String> keys = uidList.stream()
                .map(uid ->
                        RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid))
                .collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }

    public void refreshUserModifyTime(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid);
        RedisUtils.set(key, new Date().getTime());
    }

    @Cacheable(cacheNames = "user", key = "'blackList'")
    public Map<Integer, Set<String>> getBlackMap() {
        Map<Integer, List<Black>> collect = blackDao.list()
                .stream()
                .collect(Collectors.groupingBy(Black::getType));
        Map<Integer, Set<String>> result = new HashMap<>();
        collect.forEach((type, list) -> {
            result.put(type, list.stream().map(Black::getTarget).collect(Collectors.toSet()));
        });
        return result;
    }

    @CacheEvict(cacheNames = "user", key = "'blackList'")
    public Map<Integer, Set<String>> evictBlackMap() {
        return null;
    }

    @Cacheable(cacheNames = "user", key = "'roles:'+#uid")
    public Set<Long> getRoleSet(Long uid) {
        List<UserRole> userRoles = userRoleDao.listByUid(uid);
        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());
    }
}
