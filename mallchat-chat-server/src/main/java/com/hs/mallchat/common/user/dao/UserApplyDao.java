package com.hs.mallchat.common.user.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hs.mallchat.common.user.domain.entity.UserApply;
import com.hs.mallchat.common.user.domain.enums.ApplyReadStatusEnum;
import com.hs.mallchat.common.user.domain.enums.ApplyStatusEnum;
import com.hs.mallchat.common.user.domain.enums.ApplyTypeEnum;
import com.hs.mallchat.common.user.mapper.UserApplyMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hs.mallchat.common.user.domain.enums.ApplyStatusEnum.AGREE;

/**
 * <p>
 * 用户申请表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-06-24
 */
@Service
public class UserApplyDao extends ServiceImpl<UserApplyMapper, UserApply> {

    /**
     * 查询用户的添加好友申请分页信息。
     * <p>
     * 本方法用于获取指定用户（通过uid标识）的添加好友申请的分页数据。通过对申请表（UserApply）进行查询，
     * 筛选出目标用户ID为uid且申请类型为添加好友的记录，并按创建时间降序排列。最后，根据提供的Page对象进行分页。
     *
     * @param uid  用户ID，用于查询该用户收到的添加好友申请。
     * @param page 分页参数，包含当前页码和每页记录数等信息。
     * @return 返回一个IPage<UserApply>对象，其中包含了查询结果的分页信息和申请数据。
     */
    public IPage<UserApply> friendApplyPage(Long uid, Page page) {
        // 使用LambdaQueryWrapper构造查询条件，查询目标ID为uid且类型为添加好友的申请
        // 并按创建时间降序排序。最后根据提供的分页参数进行分页查询。
        return lambdaQuery()
                .eq(UserApply::getTargetId, uid)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .orderByDesc(UserApply::getCreateTime)
                .page(page);
    }

    public void readApples(Long uid, List<Long> applyIds) {

        lambdaUpdate()
                .set(UserApply::getReadStatus, ApplyReadStatusEnum.READ.getCode())
                .eq(UserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())
                .in(UserApply::getId, applyIds)
                .eq(UserApply::getTargetId, uid)
                .update();

    }

    public Integer getUnReadCount(Long targetId) {

        return lambdaQuery()
                .eq(UserApply::getTargetId, targetId)
                .eq(UserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())
                .count();

    }

    public UserApply getFriendApproving(Long uid, Long targetUid) {

        return lambdaQuery()
                .eq(UserApply::getUid, uid)
                .eq(UserApply::getTargetId, targetUid)
                .eq(UserApply::getStatus, ApplyStatusEnum.WAIT_APPROVAL)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .one();

    }

    public void agree(Long applyId) {
        lambdaUpdate()
                .set(UserApply::getStatus, AGREE.getCode())
                .eq(UserApply::getId, applyId)
                .update();

    }
}
