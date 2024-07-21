package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-05-28
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User> {

    public User getByOpenId(String openId) {
        return lambdaQuery()
                .eq(User::getOpenId, openId)
                .one();
    }

    public User getByName(String name) {
        return lambdaQuery()
                .eq(User::getName, name)
                .one();
    }

    public void modifyName(Long uid, String name) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getName, name)
                .update();
    }

    public void wearingBadge(Long uid, Long itemId) {

        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getItemId, itemId)
                .update();
    }

    public void invalidUid(Long id) {
        lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getStatus, YesOrNoEnum.YES.getStatus())
                .update();

    }

    public List<User> getFriendList(List<Long> uids) {
        return lambdaQuery()
                .in(User::getId, uids)
                .select(User::getId, User::getActiveStatus, User::getName, User::getAvatar)
                .list();
    }
}
