package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.dto.MsgReadInfoDTO;
import com.hs.mallchat.common.chat.domain.entity.Contact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.mallchat.common.chat.domain.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 会话列表 服务类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-27
 */
public interface ContactService{

    Map<Long, MsgReadInfoDTO> getMsgReadInfo(List<Message> messages);
}
