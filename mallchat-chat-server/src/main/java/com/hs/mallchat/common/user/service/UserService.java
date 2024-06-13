package com.hs.mallchat.common.user.service;

import com.hs.mallchat.common.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.mallchat.common.user.domain.vo.response.BadgeResp;
import com.hs.mallchat.common.user.domain.vo.response.UserInfoResp;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-05-28
 */
public interface UserService {

    Long registered(User insert);

    UserInfoResp getUserInfo(Long uid);

    void modifyName(Long uid, String name);

    List<BadgeResp> badges(Long uid);


    void wearingBadge(Long uid, Long itemId);
}
