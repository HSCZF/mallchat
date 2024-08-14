package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordBs;
import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.event.UserBlackEvent;
import com.hs.mallchat.common.common.event.UserRegisterEvent;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.dao.BlackDao;
import com.hs.mallchat.common.user.dao.ItemConfigDao;
import com.hs.mallchat.common.user.dao.UserBackpackDao;
import com.hs.mallchat.common.user.dao.UserDao;
import com.hs.mallchat.common.user.domain.dto.ItemInfoDTO;
import com.hs.mallchat.common.user.domain.dto.SummeryInfoDTO;
import com.hs.mallchat.common.user.domain.entity.Black;
import com.hs.mallchat.common.user.domain.entity.ItemConfig;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.hs.mallchat.common.user.domain.enums.ItemEnum;
import com.hs.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hs.mallchat.common.user.domain.vo.request.user.BlackReq;
import com.hs.mallchat.common.user.domain.vo.request.user.ItemInfoReq;
import com.hs.mallchat.common.user.domain.vo.request.user.SummeryInfoReq;
import com.hs.mallchat.common.user.domain.vo.response.user.BadgeResp;
import com.hs.mallchat.common.user.domain.vo.response.user.UserInfoResp;
import com.hs.mallchat.common.user.service.UserService;
import com.hs.mallchat.common.user.service.adapter.UserAdapter;
import com.hs.mallchat.common.user.service.cache.ItemCache;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.user.service.cache.UserSummaryCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.annotations.Cacheable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private UserCache userCache;
    @Autowired
    private UserSummaryCache userSummaryCache;
    @Autowired
    private ItemConfigDao itemConfigDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private BlackDao blackDao;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;



    @Override
    public void wearingBadge(Long uid, Long itemId) {
        // 确保有徽章
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid, itemId);
        AssertUtil.isNotEmpty(firstValidItem, "您还没有徽章，快去获得吧！");
        // 确保这物品是徽章
        ItemConfig itemConfig = itemConfigDao.getById(firstValidItem.getItemId());
        AssertUtil.equal(itemConfig.getType(), ItemTypeEnum.BADGE.getType(), "只有徽章才能佩戴！");
        // 佩戴徽章
        userDao.wearingBadge(uid, itemId);
    }

    /**
     * 用户注册，需要获得id
     *
     * @param user
     */
    @Override
    public void register(User user) {
        userDao.save(user);
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this, user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void black(BlackReq req) {
        Long uid = req.getUid();
        Black user = new Black();
        user.setType(BlackTypeEnum.UID.getType());
        user.setTarget(uid.toString());
        blackDao.save(user);
        User byId = userDao.getById(uid);
        blackIp(byId.getIpInfo().getCreateIp());
        blackIp(byId.getIpInfo().getUpdateIp());
        applicationEventPublisher.publishEvent(new UserBlackEvent(this, byId));
    }

    /**
     * 获取用户汇总信息
     *
     * @param req
     */
    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        //需要前端同步的uid
        List<Long> uidList = getNeedSyncUidList(req.getReqList());
        //加载用户信息
        Map<Long, SummeryInfoDTO> batch = userSummaryCache.getBatch(uidList);
        return req.getReqList()
                .stream()
                .map(a -> batch.containsKey(a.getUid()) ? batch.get(a.getUid()) : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 批量获取用户徽章信息
     *
     * @param req
     * @return
     */
    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {
        // 简单做，更新时间可判断被修改，不需要批量缓存框架了，徽章就几个而已
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId());
            // itemConfig.getUpdateTime().getTime()，时间返回Long类型
            if (Objects.nonNull(a.getLastModifyTime()) && a.getLastModifyTime() >= itemConfig.getUpdateTime().getTime()) {
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {
        List<Long> needSyncUidList = new ArrayList<>();
        List<Long> userModifyTime = userCache.getUserModifyTime(
                reqList.stream()
                        .map(SummeryInfoReq.infoReq::getUid)
                        .collect(Collectors.toList())
        );
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            if (Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())) {
                needSyncUidList.add(infoReq.getUid());
            }
        }
        return needSyncUidList;
    }

    private void blackIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return;
        }
        try {
            Black insert = new Black();
            insert.setType(BlackTypeEnum.IP.getType());
            insert.setTarget(ip);
            blackDao.save(insert);
        } catch (Exception e) {

        }
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
    @RedissonLock(key = "#uid")
    public void modifyName(Long uid, String name) {
        // 判断名字中有没有敏感词
        AssertUtil.isFalse(sensitiveWordBs.hasSensitiveWord(name), "名字中包含敏感词，请重新输入");
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


}
