package com.hs.mallchat.common.user.service.adapter;

import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hs.mallchat.common.user.domain.entity.ItemConfig;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.domain.vo.response.user.BadgeResp;
import com.hs.mallchat.common.user.domain.vo.response.user.UserInfoResp;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: CZF
 * @Create: 2024/5/30 - 10:34
 * Description: 用户适配器
 */
public class UserAdapter {


    /**
     * 根据openid构建用户对象，用于保存用户信息。
     *
     * @param openId 用户的openid，用于唯一标识用户。
     * @return User对象，包含openid信息。
     */
    public static User buildUserSave(String openId) {
        return User.builder().openId(openId).build();
    }

    /**
     * 根据用户ID和微信OAuth2用户信息构建授权用户对象。
     *
     * @param uid      用户的唯一标识ID。
     * @param userInfo 微信OAuth2提供的用户信息。
     * @return User对象，包含用户的基本信息。
     */
    public static User buildAuthorizeUser(Long uid, WxOAuth2UserInfo userInfo) {
        User user = new User();
        user.setId(uid);
        user.setName(userInfo.getNickname());
        user.setAvatar(userInfo.getHeadImgUrl());
        return user;
    }

    /**
     * 根据用户对象和修改名称的次数构建用户信息响应对象。
     *
     * @param user            用户对象，包含用户的基本信息。
     * @param modifyNameCount 用户剩余的修改名称的次数。
     * @return UserInfoResp对象，包含用户信息和修改名称的次数。
     */
    public static UserInfoResp buildUserInfo(User user, Integer modifyNameCount) {
        UserInfoResp vo = new UserInfoResp();
        BeanUtils.copyProperties(user, vo);
        vo.setId(user.getId());
        vo.setModifyNameChance(modifyNameCount);
        return vo;
    }

    /**
     * 根据物品配置、用户背包和用户信息，构建徽章响应对象列表。
     * 徽章响应对象包含徽章信息及用户是否获取和是否佩戴的标识。
     *
     * @param itemConfigs 物品配置列表，包含所有徽章的信息。
     * @param backpacks 用户的背包列表，记录了用户已获取的徽章。
     * @param user 用户信息，记录了用户当前佩戴的徽章。
     * @return 返回一个徽章响应对象的列表，按照佩戴和获取状态降序排序。
     */
    public static List<BadgeResp> buildBdgeResp(List<ItemConfig> itemConfigs, List<UserBackpack> backpacks, User user) {
        // 收集用户背包中所有徽章的ID，用于后续判断用户是否获取了徽章
        Set<Long> obtainItemSet = backpacks.stream()
                .map(UserBackpack::getItemId)
                .collect(Collectors.toSet());

        // 对物品配置进行流式处理，构建并返回徽章响应对象列表
        return itemConfigs.stream().map(a -> {
                    BadgeResp resp = new BadgeResp();
                    // 将物品配置信息复制到徽章响应对象
                    BeanUtils.copyProperties(a, resp);
                    // 根据用户背包中的ID集合，判断用户是否获取了该徽章，并设置相应状态
                    resp.setObtain(obtainItemSet.contains(a.getId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
                    // 根据用户当前佩戴的徽章ID，判断用户是否正在佩戴该徽章，并设置相应状态
                    resp.setWearing(Objects.equals(a.getId(), user.getItemId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
                    return resp;
                })
                // 按照佩戴和获取状态降序排序
                .sorted(Comparator.comparing(BadgeResp::getWearing, Comparator.reverseOrder())
                        .thenComparing(BadgeResp::getObtain, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }


}
