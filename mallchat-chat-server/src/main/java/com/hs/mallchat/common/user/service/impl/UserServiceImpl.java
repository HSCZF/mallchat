package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.exception.BusinessException;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.dao.ItemConfigDao;
import com.hs.mallchat.common.user.dao.UserBackpackDao;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.entity.ItemConfig;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.domain.enums.ItemEnum;
import com.hs.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hs.mallchat.common.user.domain.vo.response.BadgeResp;
import com.hs.mallchat.common.user.domain.vo.response.UserInfoResp;
import com.hs.mallchat.common.user.service.UserService;
import com.hs.mallchat.common.user.service.adapter.UserAdapter;
import com.hs.mallchat.common.user.service.cache.ItemCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.Cacheable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private ItemCache itemCache;

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Override
    public void wearingBadge(Long uid, Long itemId) {
        // 确保有徽章
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid, itemId);
        AssertUtil.isNotEmpty(firstValidItem, "您还没有徽章，快去获得吧！");
        // 确保这物品是徽章
        ItemConfig itemConfig = itemConfigDao.getById(firstValidItem.getItemId());
        AssertUtil.equal(itemConfig.getType(),ItemTypeEnum.BADGE.getType(),"只有徽章才能佩戴！");
        // 佩戴徽章
        userDao.wearingBadge(uid, itemId);
    }

    /**
     * 可选徽章预览
     *
     * @param uid
     * @return
     */
    @Override
    @Cacheable(value = "badges")
    public List<BadgeResp> badges(Long uid) {
        // 获取徽章
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        // 查询用户拥有的徽章
        List<Long> list = itemConfigs.stream()
                .map(ItemConfig::getId)
                .collect(Collectors.toList());
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uid, list);
        // 用户当前佩戴的是哪一个徽章
        User user = userDao.getById(uid);

        return UserAdapter.buildBdgeResp(itemConfigs, backpacks, user);
    }

    /**
     * 修改用户名
     *
     * @param uid
     * @param name
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void modifyName(Long uid, String name) {
        User oldUser = userDao.getByName(name);
        AssertUtil.isEmpty(oldUser, "改名失败，改名用户名已存在");
        UserBackpack modifyNameItem = userBackpackDao.getFirstValidItem(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isNotEmpty(modifyNameItem, "改名失败，改名卡不足，等送");
        boolean success = userBackpackDao.userItem(modifyNameItem);
        if (success) {
            userDao.modifyName(uid, name);
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
