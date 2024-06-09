package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.common.domain.enums.YerOrNoEnum;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.mapper.UserBackpackMapper;
import com.hs.mallchat.common.user.service.IUserBackpackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户背包表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-06-05
 */
@Service
public class UserBackpackDao extends ServiceImpl<UserBackpackMapper, UserBackpack> {

    public Integer getCountByValidItemId(Long uid, Long itemId) {
        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YerOrNoEnum.NO.getStatus())
                .count();
    }

    public UserBackpack getFirstValidItem(Long uid, Long itemId) {

        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YerOrNoEnum.NO.getStatus())
                .orderByAsc(UserBackpack::getId)
                .last("limit 1")
                .one();

    }

    public boolean userItem(UserBackpack modifyNameItem) {

        return lambdaUpdate()
                .eq(UserBackpack::getId, modifyNameItem.getId())
                .eq(UserBackpack::getStatus, YerOrNoEnum.NO.getStatus())
                .set(UserBackpack::getStatus, YerOrNoEnum.YES.getStatus())
                .update();


    }
}
