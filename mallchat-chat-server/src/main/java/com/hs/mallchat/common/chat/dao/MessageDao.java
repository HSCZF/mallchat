package com.hs.mallchat.common.chat.dao;

import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.mapper.MessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
