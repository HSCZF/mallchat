package com.hs.mallchat.common.chat.mapper;

import com.hs.mallchat.common.chat.domain.entity.Contact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 Mapper 接口
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-27
 */
public interface ContactMapper extends BaseMapper<Contact> {

    void refreshOrCreateActiveTime(@Param("roomId") Long roomId,
                                   @Param("memberUidList") List<Long> memberUidList,
                                   @Param("msgId") Long msgId,
                                   @Param("activeTime") Date activeTime);
}
