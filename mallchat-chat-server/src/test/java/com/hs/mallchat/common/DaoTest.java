package com.hs.mallchat.common;

import com.hs.mallchat.common.common.utils.JwtUtils;
import com.hs.mallchat.common.common.utils.RedisUtils;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hs.mallchat.common.user.domain.enums.ItemEnum;
import com.hs.mallchat.common.user.service.IUserBackpackService;
import com.hs.mallchat.common.user.service.LoginService;
import com.hs.mallchat.oss.MinIOTemplate;
import com.hs.mallchat.oss.domain.OssReq;
import com.hs.mallchat.oss.domain.OssResp;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.rmi.server.UID;

/**
 * @Author: CZF
 * @Create: 2024/5/28 - 15:55
 */

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class DaoTest {

    private static final long UID = 20024L;

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
    @Autowired
    private IUserBackpackService iUserBackpackService;
    @Autowired
    private MinIOTemplate minIOTemplate;

    @Test
    public void jwt() {
        String login = loginService.login(UID);
        System.out.println(login);
    }

    /**
     * 测试在minio下载文件
     */
    @Test
    public void getUploadUrl() {
        OssReq ossReq = OssReq.builder()
                .fileName("test.jpeg")
                .filePath("/test")
                .autoPath(false)
                .build();
        OssResp preSignedObjectUrl = minIOTemplate.getPreSignedObjectUrl(ossReq);
        System.out.println(preSignedObjectUrl);
    }

    @Test
    public void testAcquireItemRedisson() {
        iUserBackpackService.acquireItem(UID, ItemEnum.PLANET.getId(), IdempotentEnum.UID, UID + "");
    }


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

    @Test
    public void testRedis2() {
        String s = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjIwMDIxLCJjcmVhdGVUaW1lIjoxNzE3Mzg2NDQ5fQ.p_ckZOxGT_ZUUo5vwB7LJs-QRid-xINsf-Hcf3wzH_E";
        Long uid = loginService.getValidUid(s);
        System.out.println(uid);
    }

    @Test
    public void testWxService() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1, 1000);
        String url = wxMpQrCodeTicket.getUrl();
        System.out.println(url);
    }

}
