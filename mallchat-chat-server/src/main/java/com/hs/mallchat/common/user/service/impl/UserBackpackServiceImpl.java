package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.domain.enums.YerOrNoEnum;
import com.hs.mallchat.common.common.service.LockService;
import com.hs.mallchat.common.user.dao.UserBackpackDao;
import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import com.hs.mallchat.common.user.domain.enums.IdempotentEnum;
import com.hs.mallchat.common.user.service.IUserBackpackService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 给用户发放指定物品，确保操作的幂等性。
     * 即使同一请求重复发送，也不会导致物品被重复添加到用户的背包中。
     *
     * @param uid            用户ID。
     * @param itemId         物品ID。
     * @param idempotentEnum 幂等类型枚举，决定幂等处理方式。
     * @param businessId     业务唯一标识，用于构造幂等键。
     */
    @Override
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        // 构造幂等键
        String idempotentKey = getIdempotent(itemId, idempotentEnum, businessId);
        // 使用分布式锁保护并发操作
        lockService.executeWithLock("acquireItem" + idempotentKey, () -> {
            // 查询是否已发放过该物品
            UserBackpack existingBackpack = userBackpackDao.getByIdempotent(idempotentKey);
            if (Objects.nonNull(existingBackpack)) {
                // 已存在相同的幂等记录，直接结束避免重复处理
                return;
            }
            // 构建待插入的背包记录
            UserBackpack newItem = UserBackpack.builder()
                    .uid(uid)
                    .itemId(itemId)
                    .status(YerOrNoEnum.NO.getStatus()) // 假设NO状态表示未使用
                    .idempotent(idempotentKey)
                    .build();
            // 保存新物品到用户背包
            userBackpackDao.save(newItem);
        });
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
