package com.hs.mallchat.common.user.service;

import com.hs.mallchat.common.user.domain.dto.ItemInfoDTO;
import com.hs.mallchat.common.user.domain.dto.SummeryInfoDTO;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.vo.request.user.BlackReq;
import com.hs.mallchat.common.user.domain.vo.request.user.ItemInfoReq;
import com.hs.mallchat.common.user.domain.vo.request.user.SummeryInfoReq;
import com.hs.mallchat.common.user.domain.vo.response.user.BadgeResp;
import com.hs.mallchat.common.user.domain.vo.response.user.UserInfoResp;

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

    void black(BlackReq req);

    /**
     * 获取用户汇总信息
     */
    List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req);

    List<ItemInfoDTO> getItemInfo(ItemInfoReq req);
}
