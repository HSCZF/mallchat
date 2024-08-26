package com.hs.mallchat.common.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;
import com.hs.mallchat.common.chat.domain.enums.MessageStatusEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.mapper.MessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.utils.CursorUtils;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-22
 */
@Service
public class MessageDao extends ServiceImpl<MessageMapper, Message> {

    @Resource
    private RoomGroupDao roomGroupDao;
    @Resource
    private RoomDao roomDao;

    public void invalidByUid(Long uid) {
        lambdaUpdate()
                .eq(Message::getFromUid, uid)
                .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus())
                .update();
    }

    public CursorPageBaseResp<Message> getCursorPage(Long roomId, CursorPageBaseReq request, Long lastMsgId) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(Message::getRoomId, roomId);
            wrapper.eq(Message::getStatus, MessageStatusEnum.NORMAL.getStatus());
            wrapper.le(Objects.nonNull(lastMsgId), Message::getId, lastMsgId);
        }, Message::getId);
    }

    public Integer getGapCount(Long roomId, Long fromId, Long toId) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .gt(Message::getId, fromId)
                .le(Message::getId, toId)
                .count();
    }

    public Integer getUnReadCount(Long roomId, Date readTime) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .gt(Objects.nonNull(readTime), Message::getCreateTime, readTime)
                .count();
    }

    public Boolean removeByRoomId(Long roomId, List<Long> uidList) {
        if (CollectionUtil.isNotEmpty(uidList)) {
            LambdaUpdateWrapper<Message> wrapper = new UpdateWrapper<Message>().lambda()
                    .eq(Message::getRoomId, roomId)
                    .in(Message::getFromUid, uidList)
                    .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus());
            return this.update(wrapper);
        } else {
            // 群主未发送过消息，直接返回true
            if (!hasMessagesSentByOwner(roomId)) {
                return true;
            }
            LambdaUpdateWrapper<Message> wrapper = new UpdateWrapper<Message>().lambda()
                    .eq(Message::getRoomId, roomId)
                    .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus());
            // 执行更新操作
            return this.update(wrapper);
        }
    }

    /**
     * 检查群主是否发送过消息。
     *
     * @param roomId 房间ID
     * @return 是否发送过消息
     */
    public boolean hasMessagesSentByOwner(Long roomId) {
        // 群主id
        Long uid = RequestHolder.get().getUid();
        // ne() -> 排除状态为 NORMAL 的消息。
        return this.count(new QueryWrapper<Message>().lambda()
                .eq(Message::getRoomId, roomId)
                .eq(Message::getFromUid, uid)
                .ne(Message::getStatus, MessageStatusEnum.DELETE.getStatus())) > 0;
    }

    public Message getByUidAndRoomId(Long roomId, Long fromUid) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .eq(Message::getFromUid, fromUid)
                .one();
    }
}
