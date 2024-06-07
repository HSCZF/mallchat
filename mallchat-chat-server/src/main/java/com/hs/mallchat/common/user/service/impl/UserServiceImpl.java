package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.exception.BusinessException;
import com.hs.mallchat.common.user.dao.UserBackpackDao;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.ItemEnum;
import com.hs.mallchat.common.user.domain.vo.response.UserInfoResp;
import com.hs.mallchat.common.user.service.UserService;
import com.hs.mallchat.common.user.service.adapter.UserAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 10:40
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackDao userBackpackDao;

    /**
     * 修改用户名
     *
     * @param uid
     * @param name
     */
    @Override
    public void modifyName(Long uid, String name) {
        User oldUser = userDao.getByName(name);
        if(Objects.nonNull(oldUser)){
            // todo 改名
            throw new BusinessException("改名失败，改名用户名已存在");
        }
    }

    /**
     * 获取用户信息
     *
     * @param uid
     * @return
     */
    @Override
    public UserInfoResp getUserInfo(Long uid) {
        User user = userDao.getById(uid);
        Integer modifyNameCount = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        return UserAdapter.buildUserInfo(user, modifyNameCount);
    }

    /**
     * 开个事务
     *
     * @param insert
     * @return
     */
    @Override
    @Transactional
    public Long registered(User insert) {
        boolean save = userDao.save(insert);
        // 用户注册的事件
        return insert.getId();
    }
}
