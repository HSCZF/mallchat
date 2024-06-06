package com.hs.mallchat.common;

import com.hs.mallchat.common.common.utils.JwtUtils;
import com.hs.mallchat.common.common.utils.RedisUtils;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

/**
 * @Author: CZF
 * @Create: 2024/5/28 - 15:55
 */

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class DaoTest {

    @Autowired
    private UserDao userDao;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private LoginService loginService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    public void test() {
        User user = new User();
        user.setName("133");
        user.setOpenId("1334");
        //
        User user2 = User.builder()
                .name("1")
                .openId("2")
                .build();
        System.out.println(user);
        System.out.println(user2);
        boolean save = userDao.save(user);
        System.out.println(save);
    }

    @Test
    public void testWxService() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1, 1000);
        String url = wxMpQrCodeTicket.getUrl();
        System.out.println(url);
    }

    @Test
    public void testJwt() throws InterruptedException {
        // 测试jwt
        System.out.println(jwtUtils.createToken(1L));
        Thread.currentThread().sleep(2000);
        System.out.println(jwtUtils.createToken(1L));
        Thread.currentThread().sleep(2000);
        System.out.println(jwtUtils.createToken(1L));
        Thread.currentThread().sleep(2000);
        System.out.println(jwtUtils.createToken(1L));

    }

    @Test
    public void testRedis() {
        RedisUtils.set("name", "欧皇大大");
        String name = RedisUtils.getStr("name");
        System.out.println(name);
    }

    @Test
    public void testRedis2() {
        String s = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjIwMDIxLCJjcmVhdGVUaW1lIjoxNzE3Mzg2NDQ5fQ.p_ckZOxGT_ZUUo5vwB7LJs-QRid-xINsf-Hcf3wzH_E";
        Long uid = loginService.getValidUid(s);
        System.out.println(uid);
    }

    @Test
    public void thread() throws InterruptedException {
        // 测试自定义线程池的异常捕获
        threadPoolTaskExecutor.execute(() -> {
        if (1 == 1) {
            log.error("12345");
            throw new RuntimeException("12345678");
        }
        });
        Thread.sleep(300);
    }

}
