package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hs.mallchat.common.common.service.LockService;
import com.hs.mallchat.common.user.dao.UserBackpackDao;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hs.mallchat.common.user.service.IUserBackpackService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 分布式锁-编程式
 * 用户背包服务实现类，负责处理与用户背包相关的业务逻辑，如物品的发放与管理。
 * 实现了幂等性发放物品功能，确保同一请求多次执行结果一致，避免重复发放。
 *
 * @Author: CZF
 * @Date: 2024/6/13
 * @Time: 9:21 AM
 */
@Service
public class UserBackpackServiceImpl implements IUserBackpackService {

    @Autowired
    private RedissonClient redissonClient; // Redisson客户端，用于分布式锁操作
    @Autowired
    private LockService lockService; // 分布式锁服务，封装了加锁解锁逻辑
    @Autowired
    private UserBackpackDao userBackpackDao; // 用户背包数据访问接口
    @Autowired
    @Lazy
    private UserBackpackServiceImpl userBackpackService;

//    /**
//     * 分布式-编程式
//     * 给用户发放指定物品，确保操作的幂等性。
//     * 即使同一请求重复发送，也不会导致物品被重复添加到用户的背包中。
//     *
//     * @param uid            用户ID。
//     * @param itemId         物品ID。
//     * @param idempotentEnum 幂等类型枚举，决定幂等处理方式。
//     * @param businessId     业务唯一标识，用于构造幂等键。
//     */
//    @Override
//    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
//        // 构造幂等键
//        String idempotentKey = getIdempotent(itemId, idempotentEnum, businessId);
//        // 使用分布式锁保护并发操作
//        lockService.executeWithLock("acquireItem" + idempotentKey, () -> {
//            // 查询是否已发放过该物品
//            UserBackpack existingBackpack = userBackpackDao.getByIdempotent(idempotentKey);
//            if (Objects.nonNull(existingBackpack)) {
//                // 已存在相同的幂等记录，直接结束避免重复处理
//                return;
//            }
//            // 构建待插入的背包记录
//            UserBackpack newItem = UserBackpack.builder()
//                    .uid(uid)
//                    .itemId(itemId)
//                    .status(YerOrNoEnum.NO.getStatus()) // 假设NO状态表示未使用
//                    .idempotent(idempotentKey)
//                    .build();
//            // 保存新物品到用户背包
//            userBackpackDao.save(newItem);
//        });
//    }

    /**
     * 用户获取物品的接口。
     *
     * 本方法用于处理用户获取特定物品的逻辑。通过传入用户ID、物品ID和幂等性标识，确保同一用户对同一物品的获取操作是幂等的。
     * 即多次调用对系统的影响与调用一次相同。幂等性通过生成唯一的幂等键来实现，该键基于物品ID、幂等性枚举和业务ID生成。
     *
     * @param uid 用户的唯一标识。用于指定物品获取操作的用户。
     * @param itemId 物品的唯一标识。指定用户要获取的物品。
     * @param idempotentEnum 幂等性枚举。用于指示如何生成幂等键，以避免重复操作。
     * @param businessId 业务标识。可以是订单ID或其他业务流程的标识，用于生成幂等键，确保在同一个业务流程中操作的幂等性。
     */
    @Override
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        // 根据物品ID、幂等性枚举和业务ID生成幂等键
        String idempotent = getIdempotent(itemId, idempotentEnum, businessId);
        // 调用背包服务，执行物品的获取操作，传入用户ID、物品ID和幂等键
        // 直接使用doAcquireItem(uid, itemId, idempotent);会使@Transactional失效，所以需要依赖注入userBackpackService去调用
        // 但是这样又会循环依赖，在依赖注入的时候，还需要加入注解 @Lazy
        userBackpackService.doAcquireItem(uid, itemId, idempotent);
    }

    /**
     * 分布式-注解式
     * 尝试为用户获取物品并放入背包中。
     * 此方法使用Redisson锁来保证并发环境下的幂等性，即同一个用户对同一个物品的获取操作只会成功一次。
     *
     * @param uid        用户ID，表示物品的归属用户。
     * @param itemId     物品ID，表示需要获取的具体物品。
     * @param idempotent 幂等标识，用于确保相同操作的幂等性，即重复操作不会产生副作用。
     */
    @RedissonLock(key = "#idempotent", waitTime = 5000)
    public void doAcquireItem(Long uid, Long itemId, String idempotent) {
        // 根据幂等标识尝试获取用户背包中的物品，如果已存在则直接返回，避免重复添加。
        UserBackpack userBackpack = userBackpackDao.getByIdempotent(idempotent);
        if (Objects.nonNull(userBackpack)) {
            return;
        }
        // 如果背包中不存在该物品，则创建一个新的物品记录，设置用户ID、物品ID、状态为未使用，并保存到数据库中。
        // 发放物品
        UserBackpack newItem = UserBackpack.builder()
                .uid(uid)
                .itemId(itemId)
                .status(YesOrNoEnum.NO.getStatus())
                .idempotent(idempotent)
                .build();
        userBackpackDao.save(newItem);
    }


    /**
     * 根据物品ID、幂等类型及业务ID生成幂等键字符串。
     * 用于确保相同逻辑的请求具有唯一的标识，便于幂等控制。
     *
     * @param itemId         物品ID。
     * @param idempotentEnum 幂等类型枚举。
     * @param businessId     业务唯一标识。
     * @return 构造的幂等键字符串。
     */
    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        return String.format("%d_%d_%s", itemId, idempotentEnum.getType(), businessId);
    }
}
