package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.mapper.UserBackpackMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .count();
    }

    public UserBackpack getFirstValidItem(Long uid, Long itemId) {

        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .orderByAsc(UserBackpack::getId)
                .last("limit 1")
                .one();

    }

    public boolean userItem(UserBackpack modifyNameItem) {

        return lambdaUpdate()
                .eq(UserBackpack::getId, modifyNameItem.getId())
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .set(UserBackpack::getStatus, YesOrNoEnum.YES.getStatus())
                .update();


    }

    public List<UserBackpack> getByItemIds(Long uid, List<Long> itemIds) {

        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .in(UserBackpack::getItemId, itemIds)
                .list();

    }
    public List<UserBackpack> getByItemIds(List<Long> uids, List<Long> itemIds) {

        return lambdaQuery().in(UserBackpack::getUid, uids)
                .in(UserBackpack::getItemId, itemIds)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .list();

    }

    public UserBackpack getByIdempotent(String idempotent) {

        return lambdaQuery()
                .eq(UserBackpack::getIdempotent, idempotent)
                .one();

    }
}
