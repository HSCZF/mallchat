package com.hs.mallchat.common.chat.dao;

import com.hs.mallchat.common.chat.domain.entity.Contact;
import com.hs.mallchat.common.chat.mapper.ContactMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-27
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact> {

    public void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long msgId, Date activeTime) {
        baseMapper.refreshOrCreateActiveTime(roomId, memberUidList, msgId, activeTime);
    }

    public Contact get(Long uid, Long roomId) {
        return lambdaQuery()
                .eq(Contact::getUid, uid)
                .eq(Contact::getRoomId, roomId)
                .one();
    }
}
