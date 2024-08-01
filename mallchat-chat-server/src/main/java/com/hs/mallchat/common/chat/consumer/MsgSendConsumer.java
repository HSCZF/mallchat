package com.hs.mallchat.common.chat.consumer;

import com.hs.mallchat.common.chat.dao.ContactDao;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.dao.RoomDao;
import com.hs.mallchat.common.chat.dao.RoomFriendDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.chat.domain.entity.RoomFriend;
import com.hs.mallchat.common.chat.domain.enums.RoomTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageResp;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.chat.service.cache.GroupMemberCache;
import com.hs.mallchat.common.chat.service.cache.HotRoomCache;
import com.hs.mallchat.common.chat.service.cache.RoomCache;
import com.hs.mallchat.common.common.constant.MQConstant;
import com.hs.mallchat.common.common.domain.dto.MsgSendMessageDTO;
import com.hs.mallchat.common.user.service.adapter.WSAdapter;
import com.hs.mallchat.common.user.service.impl.PushService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Description： 发送消息更新房间收信箱，并同步给房间的成员信箱
 *
 * @Author: CZF
 * @Create: 2024/7/27 - 20:22
 */
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_MSG_GROUP, topic = MQConstant.SEND_MSG_TOPIC)
@Component
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {

    @Autowired
    private ChatService chatService;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private PushService pushService;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private ContactDao contactDao;

    /**
     * 当收到发送消息的DTO时的处理方法。
     * 根据消息ID获取消息对象和房间对象，然后根据房间的类型和热度来决定消息的推送范围。
     * 同时，更新房间和相关用户的活跃时间，并推送消息。
     *
     * @param dto 消息发送的DTO，包含消息ID等信息。
     */
    @Override
    public void onMessage(MsgSendMessageDTO dto) {
        // 根据消息ID获取消息对象
        Message message = messageDao.getById(dto.getMsgId());
        // 根据消息的房间ID获取房间对象
        Room room = roomCache.get(message.getRoomId());
        // 根据消息对象生成聊天消息响应对象，组装给前端
        ChatMessageResp msgResp = chatService.getMsgResp(message, null);
        // 更新房间的活跃时间，并在数据库中保存
        // 所有房间更新最新消息
        roomDao.refreshActiveTime(room.getId(), message.getId(), message.getCreateTime());
        // 从房间缓存中删除房间对象
        roomCache.delete(room.getId());
        // 如果房间是热门房间，则推送给所有在线用户
        if (room.isHotRoom()) {
            // 更新热门房间的活跃时间,热门群聊时间-redis
            hotRoomCache.refreshActiveTime(room.getId(), message.getCreateTime());
            // 构建消息并推送给所有在线用户
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp));
        } else {
            // 初始化成员UID列表
            List<Long> memberUidList = new ArrayList<>();
            // 根据房间类型决定推送范围，如果是群聊，则获取群成员UID列表
            if (Objects.equals(room.getType(), RoomTypeEnum.GROUP.getType())) {
                // 普通群聊推送所有群成员
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            } else if (Objects.equals(room.getType(), RoomTypeEnum.FRIEND.getType())) {
                // 如果是私聊，则获取私聊双方的UID
                // 单聊对象，对单人推送
                RoomFriend roomFriend = roomFriendDao.getByRoomId(room.getId());
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            // 更新相关用户的活跃时间
            // 更新所有群成员的会话时间
            contactDao.refreshOrCreateActiveTime(room.getId(), memberUidList, message.getId(), message.getCreateTime());
            // 构建消息并推送给相关用户
            pushService.sendPushMsg(WSAdapter.buildMsgSend(msgResp), memberUidList);
        }
    }
}
