package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.common.event.UserBlackEvent;
import com.hs.mallchat.common.common.event.UserOnlineEvent;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.UserActiveStatusEnum;
import com.hs.mallchat.common.user.service.IpService;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.websocket.service.WebSocketService;
import com.hs.mallchat.common.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserBlackListener {

    @Autowired
    private UserDao userDao;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserCache userCache;

    /**
     * 异步方法，用于在用户黑名单事件处理完成后，
     * 通过WebSocket向所有客户端广播黑名单更新信息。
     *
     * @param event 用户黑名单事件实例，包含被操作的用户信息。
     */
    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendMsg(UserBlackEvent event) {
        User user = event.getUser(); // 获取事件中的用户对象
        webSocketService.sendMsgToAll(WebSocketAdapter.buildBlack(user)); // 广播黑名单更新消息
    }

    /**
     * 异步方法，在用户黑名单事件处理的事务提交后，
     * 更新数据库中用户的状态为无效（如加入黑名单）。
     *
     * @param event 用户黑名单事件实例，包含被操作的用户信息。
     */
    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void changeUserStatus(UserBlackEvent event) {
        userDao.invalidUid(event.getUser().getId()); // 根据用户ID使用户无效
    }

    /**
     * 异步方法，清除缓存中与用户黑名单相关的映射信息，
     * 确保缓存与数据库状态一致。
     *
     * @param event 用户黑名单事件实例。
     */
    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void evictCache(UserBlackEvent event) {
        userCache.evictBlackMap(); // 清除黑名单缓存映射
    }


}
