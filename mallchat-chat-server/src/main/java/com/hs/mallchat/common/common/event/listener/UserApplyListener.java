package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.common.event.UserApplyEvent;
import com.hs.mallchat.common.user.dao.UserApplyDao;
import com.hs.mallchat.common.user.domain.entity.UserApply;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSFriendApply;
import com.hs.mallchat.common.user.service.WebSocketService;
import com.hs.mallchat.common.user.service.adapter.WSAdapter;
import com.hs.mallchat.common.user.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Description: 好友申请监听器
 *
 * @Author: CZF
 * @Create: 2024/8/2 - 16:32
 */
@Slf4j
@Component
public class UserApplyListener {

    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private PushService pushService;

    @Async
    @TransactionalEventListener(classes = UserApplyEvent.class, fallbackExecution = true)
    public void notifyFriend(UserApplyEvent event) {
        log.info("好友申请监听器监听到事件:{}", event);
        UserApply userApply = event.getUserApply();
        Integer unReadCount = userApplyDao.getUnReadCount(userApply.getTargetId());
        //发送消息给申请人
        pushService.sendPushMsg(WSAdapter.buildApplySend(new WSFriendApply(userApply.getUid(), unReadCount)), userApply.getTargetId());
    }


}
