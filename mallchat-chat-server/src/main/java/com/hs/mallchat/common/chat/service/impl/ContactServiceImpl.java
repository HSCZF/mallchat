package com.hs.mallchat.common.chat.service.impl;

import com.hs.mallchat.common.chat.dao.ContactDao;
import com.hs.mallchat.common.chat.domain.dto.MsgReadInfoDTO;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.service.ContactService;
import com.hs.mallchat.common.common.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * 会话列表
 *
 * @Author: CZF
 * @Create: 2024/8/21 - 9:16
 */
@Service
public class ContactServiceImpl implements ContactService {
    @Autowired
    private ContactDao contactDao;

    /**
     * 根据消息列表获取每个消息的阅读信息
     * 此方法主要用来计算每个消息的阅读人数和未阅读人数
     *
     * @param messages 消息列表，所有消息应属于同一个房间
     * @return 返回一个映射，其中包含每个消息的ID及其对应的阅读信息DTO
     */
    @Override
    public Map<Long, MsgReadInfoDTO> getMsgReadInfo(List<Message> messages) {
        // 将消息按房间ID分组
        Map<Long, List<Message>> roomGroup = messages.stream().collect(Collectors.groupingBy(Message::getRoomId));
        // 确保所有消息都属于同一个房间，否则抛出异常
        AssertUtil.equal(roomGroup.size(), 1, "只能查相同房间下的消息");
        // 获取唯一的一个房间ID
        Long roomId = roomGroup.keySet().iterator().next();
        // 查询该房间下消息的总条数
        Integer totalCount = contactDao.getTotalCount(roomId);
        // 对消息列表进行处理，生成每个消息的阅读信息DTO
        return messages.stream().map(message -> {
            MsgReadInfoDTO readInfoDTO = new MsgReadInfoDTO();
            readInfoDTO.setMsgId(message.getId());
            // 查询该消息的阅读人数
            Integer readCount = contactDao.getReadCount(message);
            readInfoDTO.setReadCount(readCount);
            // 计算该消息的未阅读人数
            readInfoDTO.setUnReadCount(totalCount - readCount - 1);
            return readInfoDTO;
        }).collect(Collectors.toMap(MsgReadInfoDTO::getMsgId, Function.identity()));
    }

}
