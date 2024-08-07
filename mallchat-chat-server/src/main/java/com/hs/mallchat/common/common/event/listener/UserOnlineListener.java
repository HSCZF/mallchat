package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.common.event.UserOnlineEvent;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.ChatActiveStatusEnum;
import com.hs.mallchat.common.user.domain.enums.UserActiveStatusEnum;
import com.hs.mallchat.common.user.service.IpService;
import com.hs.mallchat.common.user.service.adapter.WSAdapter;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.user.service.impl.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @Author: CZF
 * @Create: 2024/6/17 - 11:28
 * Description:
 */
@Component
public class UserOnlineListener {

    @Autowired
    private IpService ipService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private PushService pushService;
    @Autowired
    private WSAdapter wsAdapter;
    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveRedisAndPush(UserOnlineEvent event) {
        User user = event.getUser();
        userCache.online(user.getId(), user.getLastOptTime());
        //推送给所有在线用户，该用户登录成功
        pushService.sendPushMsg(wsAdapter.buildOnlineNotifyResp(event.getUser()));
    }

    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveDB(UserOnlineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastOptTime(user.getLastOptTime());
        update.setIpInfo(user.getIpInfo());
        update.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
        userDao.updateById(update);
        //更新用户ip详情
        ipService.refreshIpDetailAsync(user.getId());
    }


}
