package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.common.event.UserOfflineEvent;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.user.service.WebSocketService;
import com.hs.mallchat.common.user.service.adapter.WSAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Description: 用户下线监听器
 *
 * @Author: CZF
 * @Create: 2024/7/30 - 16:46
 */
@Slf4j
@Component
public class UserOfflineListener {

    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private WSAdapter wsAdapter;

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        userCache.offline(user.getId(), user.getLastOptTime());
        // 推送给所有在线用户，该用户下线
        webSocketService.sendToAllOnline(wsAdapter.buildOfflineNotifyResp(event.getUser()),event.getUser().getId());
    }

}
