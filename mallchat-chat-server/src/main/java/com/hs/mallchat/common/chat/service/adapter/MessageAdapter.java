package com.hs.mallchat.common.chat.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.MessageMark;
import com.hs.mallchat.common.chat.domain.enums.MessageMarkTypeEnum;
import com.hs.mallchat.common.chat.domain.enums.MessageStatusEnum;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.request.msg.TextMsgReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageResp;
import com.hs.mallchat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.hs.mallchat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: 消息适配器
 *
 * @Author: CZF
 * @Create: 2024/7/23 - 8:23
 */
public class MessageAdapter {

    public static final int CAN_CALLBACK_GAP_COUNT = 100;


    public static Message buildMsgSave(ChatMessageReq request, Long uid) {
        return Message.builder()
                .fromUid(uid)
                .roomId(request.getRoomId())
                .type(request.getMsgType())
                .status(MessageStatusEnum.NORMAL.getStatus())
                .build();
    }

    /**
     * 根据消息列表和标记列表，构建聊天消息响应列表。
     * 此方法用于将原始的消息和标记数据转换为前端展示所需的格式，并按照消息发送时间排序。
     *
     * @param messages   原始消息列表。
     * @param msgMark    消息标记列表，用于标识消息的阅读状态。
     * @param receiveUid 接收消息的用户ID，用于构建消息对象。
     * @return 返回排序后的聊天消息响应列表。
     */
    public static List<ChatMessageResp> buildMsgSave(List<Message> messages, List<MessageMark> msgMark, Long receiveUid) {
        // 将消息标记按消息ID分组，以便后续处理。
        Map<Long, List<MessageMark>> markMap = msgMark.stream().collect(Collectors.groupingBy(MessageMark::getMsgId));

        // 对原始消息列表进行映射和转换，构建聊天消息响应列表，并按照发送时间排序。
        return messages.stream().map(a -> {
                    ChatMessageResp resp = new ChatMessageResp();
                    // 设置消息来源用户信息。
                    resp.setFromUser(buildFromUser(a.getFromUid()));
                    // 设置消息内容和阅读状态等信息。getOrDefault用于从 Map 中获取一个键对应的值，如果该键不存在则返回一个默认值
                    resp.setMessage(buildMessage(a, markMap.getOrDefault(a.getId(), new ArrayList<>()), receiveUid));
                    return resp;
                }).sorted(Comparator.comparing(a -> a.getMessage().getSendTime())) // 按照消息发送时间排序
                .collect(Collectors.toList());
    }

    private static ChatMessageResp.UserInfo buildFromUser(Long fromUid) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUid(fromUid);
        return userInfo;
    }

    /**
     * 根据消息对象、消息标记和接收用户ID，构建聊天消息响应对象。
     *
     * @param message    消息对象，包含消息内容和属性。
     * @param marks      消息标记列表，用于标记消息的特定状态。
     * @param receiveUid 接收消息的用户ID。
     * @return 构建后的聊天消息响应对象。
     */
    private static ChatMessageResp.Message buildMessage(Message message, List<MessageMark> marks, Long receiveUid) {
        // 创建聊天消息响应对象
        ChatMessageResp.Message messageVO = new ChatMessageResp.Message();
        // 将消息对象的属性复制到聊天消息响应对象
        BeanUtil.copyProperties(message, messageVO);
        // 设置消息发送时间
        messageVO.setSendTime(message.getCreateTime());
        // 根据消息类型获取消息处理策略，比如：TXT，视频，语音，图片，文件等。
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
        // 如果消息处理策略存在，则处理消息内容
        if (Objects.nonNull(msgHandler)) {
            messageVO.setBody(msgHandler.showMsg(message));
        }
        // 构建消息标记信息，并设置到聊天消息响应对象
        // 消息标记
        messageVO.setMessageMark(buildMsgMark(marks, receiveUid));
        // 返回构建后的聊天消息响应对象
        return messageVO;
    }

    /**
     * 根据消息标记列表和接收用户的ID，构建消息标记响应对象。
     *
     * @param marks      消息标记列表，包含所有的点赞和不喜欢的标记。
     * @param receiveUid 接收消息的用户ID，可能为null。
     * @return 构建的消息标记响应对象，包含点赞和不喜欢的数量，以及用户是否点赞或不喜欢的状态。
     */
    private static ChatMessageResp.MessageMark buildMsgMark(List<MessageMark> marks, Long receiveUid) {
        // 根据标记的类型对标记进行分组，以便后续处理。
        Map<Integer, List<MessageMark>> typeMap = marks.stream().collect(Collectors.groupingBy(MessageMark::getType));

        // 获取点赞标记列表，如果不存在则初始化为空列表。
        List<MessageMark> likeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.LIKE.getType(), new ArrayList<>());

        // 获取不喜欢标记列表，如果不存在则初始化为空列表。
        List<MessageMark> dislikeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.DISLIKE.getType(), new ArrayList<>());

        // 创建消息标记响应对象。
        ChatMessageResp.MessageMark mark = new ChatMessageResp.MessageMark();
        // 设置点赞数量。
        mark.setLikeCount(likeMarks.size());
        // 设置用户点赞状态，如果用户点赞，则为YES，否则为NO。anyMatch：只要满足一个条件就直接返回true。
        mark.setUserLike(Optional.ofNullable(receiveUid)
                .filter(uid -> likeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid)))
                .map(a -> YesOrNoEnum.YES.getStatus())
                .orElse(YesOrNoEnum.NO.getStatus()));
        // 设置用户不喜欢数量。
        mark.setDislikeCount(dislikeMarks.size());
        // 设置用户不喜欢状态，如果用户不喜欢，则为YES，否则为NO。
        mark.setUserDislike(Optional.ofNullable(receiveUid)
                .filter(uid -> dislikeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid)))
                .map(a -> YesOrNoEnum.YES.getStatus())
                .orElse(YesOrNoEnum.NO.getStatus()));
        return mark;
    }


    public static ChatMessageReq buildAgreeMsg(Long roomId) {
        ChatMessageReq chatMessageReq = new ChatMessageReq();
        chatMessageReq.setRoomId(roomId);
        chatMessageReq.setMsgType(MessageTypeEnum.TEXT.getType());
        TextMsgReq textMsgReq = new TextMsgReq();
        textMsgReq.setContent("我们已经成为好友了，开始聊天吧");
        chatMessageReq.setBody(textMsgReq);
        return chatMessageReq;
    }
}
