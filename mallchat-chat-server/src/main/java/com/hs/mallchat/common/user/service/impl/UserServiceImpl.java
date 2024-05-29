package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 10:40
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDao userDao;

    /**
     * 开个事务
     * @param insert
     * @return
     */
    @Override
    @Transactional
    public Long registered(User insert) {
        boolean save = userDao.save(insert);
        // todo 用户注册的事件
        return insert.getId();
    }
}
