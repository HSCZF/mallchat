package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.user.domain.entity.UserEmoji;
import com.hs.mallchat.common.user.domain.vo.response.user.UserEmojiResp;
import com.hs.mallchat.common.user.mapper.UserEmojiMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户表情包 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-08-08
 */
@Service
public class UserEmojiDao extends ServiceImpl<UserEmojiMapper, UserEmoji> {

    public int countByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).count();
    }

    public List<UserEmoji> listByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).list();
    }
}
