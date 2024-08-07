package com.hs.mallchat.common.user.service.cache;

import com.hs.mallchat.common.common.constant.RedisKey;
import com.hs.mallchat.common.common.service.cache.AbstractRedisStringCache;
import com.hs.mallchat.common.user.dao.UserBackpackDao;
import com.hs.mallchat.common.user.domain.dto.SummeryInfoDTO;
import com.hs.mallchat.common.user.domain.entity.*;
import com.hs.mallchat.common.user.domain.enums.ItemTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * 批量的旁路缓存,用户基本信息的缓存
 *
 * @Author: CZF
 * @Create: 2024/7/2 - 11:17
 */
@Component
public class UserSummaryCache extends AbstractRedisStringCache<Long, SummeryInfoDTO> {

    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private UserBackpackDao userBackpackDao;
    @Autowired
    private ItemCache itemCache;

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_SUMMARY_STRING, uid);
    }

    @Override
    protected Long getExpireSeconds() {
        return 10 * 60L;
    }

    @Override
    protected Map<Long, SummeryInfoDTO> load(List<Long> uidList) {
        // 用户基本信息
        Map<Long, User> userMap = userInfoCache.getBatch(uidList);
        // 用户徽章信息
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        List<Long> itemIds = itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList());
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uidList, itemIds);
        Map<Long, List<UserBackpack>> userBadgeMap = backpacks.stream().collect(Collectors.groupingBy(UserBackpack::getUid));
        // 用户最后一次更换新时间
        return uidList.stream().map(uid -> {
                    SummeryInfoDTO summeryInfoDTO = new SummeryInfoDTO();
                    User user = userMap.get(uid);
                    if (Objects.isNull(user)) {
                        return null;
                    }
                    List<UserBackpack> userBackpacks = userBadgeMap.getOrDefault(user.getId(), new ArrayList<>());
                    summeryInfoDTO.setUid(user.getId());
                    summeryInfoDTO.setName(user.getName());
                    summeryInfoDTO.setAvatar(user.getAvatar());
                    summeryInfoDTO.setLocPlace(Optional.ofNullable(user.getIpInfo()).map(IpInfo::getUpdateIpDetail).map(IpDetail::getCity).orElse(null));
                    summeryInfoDTO.setWearingItemId(user.getItemId());
                    summeryInfoDTO.setItemIds(userBackpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toList()));
                    return summeryInfoDTO;
                }).filter(Objects::nonNull)
                .collect(Collectors.toMap(SummeryInfoDTO::getUid, Function.identity()));
    }
}
