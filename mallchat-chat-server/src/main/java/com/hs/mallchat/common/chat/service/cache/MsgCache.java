package com.hs.mallchat.common.chat.service.cache;

import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.user.dao.BlackDao;
import com.hs.mallchat.common.user.dao.RoleDao;
import com.hs.mallchat.common.user.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Description: 消息相关缓存
 *
 * @Author: CZF
 * @Create: 2024/8/6 - 11:16
 */
@Component
public class MsgCache {

    @Autowired
    private UserDao userDao;
    @Autowired
    private BlackDao blackDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private MessageDao messageDao;

    @Cacheable(cacheNames = "msg", key = "'msg'+#msgId")
    public Message getMsg(Long msgId) {
        return messageDao.getById(msgId);
    }

    @CacheEvict(cacheNames = "msg", key = "'msg'+#msgId")
    public Message evictMsg(Long msgId) {
        return null;
    }
}

