package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.common.event.UserRegisterEvent;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hs.mallchat.common.user.domain.enums.ItemEnum;
import com.hs.mallchat.common.user.service.IUserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户注册事件监听器，负责在用户注册后异步执行一系列操作，
 * 如发放更名卡、特殊注册徽章等，以增强用户体验并记录用户成就。
 */
@Component
public class UserRegisterListener {

    @Autowired
    private IUserBackpackService userBackpackService;
    @Autowired
    private UserDao userDao;

    /**
     * 异步监听用户注册事件，在事务提交后执行。
     * 为新注册用户发放更名卡。
     *
     * @param event 用户注册事件，包含注册用户的详细信息。
     */
    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendCard(UserRegisterEvent event) {
        User user = event.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(), IdempotentEnum.UID, user.getId().toString());
    }

    /**
     * 异步监听用户注册事件，在事务提交后执行。
     * 根据注册顺序为新用户发放专属的注册徽章。
     *
     * @param event 用户注册事件，包含注册用户的详细信息。
     */
    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendBadge(UserRegisterEvent event) {
        User user = event.getUser();
        int registerCount = userDao.count();
        // 为前10名注册用户提供特殊徽章
        if (registerCount < 10) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP10_BADGE.getId(), IdempotentEnum.UID, user.getId().toString());
        }
        // 为11至100名注册用户提供另一款特殊徽章
        else if (registerCount < 100) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP100_BADGE.getId(), IdempotentEnum.UID, user.getId().toString());
        }
    }
}
