package com.hs.mallchat.common.common.event.listener;

import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.common.event.UserBlackEvent;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.user.domain.enums.WSRespTypeEnum;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBaseResp;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBlack;
import com.hs.mallchat.common.user.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Author: CZF
 * @Create: 2024/6/17 - 11:28
 * Description:
 */
@Component
public class UserBlackListener {

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserCache userCache;

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void refreshRedis(UserBlackEvent event) {
        userCache.evictBlackMap(); // 清除黑名单缓存映射
        userCache.remove(event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void deleteMsg(UserBlackEvent event) {
        messageDao.invalidByUid(event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void sendPush(UserBlackEvent event) {
        Long uid = event.getUser().getId();
        WSBaseResp<WSBlack> resp = new WSBaseResp<>();
        WSBlack black = new WSBlack(uid);
        resp.setData(black);
        resp.setType(WSRespTypeEnum.BLACK.getType());
        webSocketService.sendToAllOnline(resp, uid);
    }

}
