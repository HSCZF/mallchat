package com.hs.mallchat.common;

import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: CZF
 * @Create: 2024/5/28 - 15:55
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class DaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void test(){
        User byID = userDao.getById(1);
        User user = new User();
        user.setName("133");
        user.setOpenId("1334");
        boolean save = userDao.save(user);
        System.out.println(save);

    }


}
