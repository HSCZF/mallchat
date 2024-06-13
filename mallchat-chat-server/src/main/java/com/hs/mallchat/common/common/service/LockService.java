package com.hs.mallchat.common.common.service;

import com.hs.mallchat.common.common.exception.BusinessException;
import com.hs.mallchat.common.common.exception.CommonErrorEnum;
import lombok.SneakyThrows;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁-编程式
 * 分布式锁服务实现类，提供编程式锁操作方法，基于Redisson客户端实现。
 * 用于在多线程或分布式环境下确保业务操作的原子性和一致性，防止并发冲突。
 *
 * @Author: CZF
 * @Date: 2024/6/13
 * @Time: 10:00 AM
 */
@Service
public class LockService {

    @Autowired
    private RedissonClient redissonClient; // Redisson客户端实例，用于操作Redis分布式锁。

    /**
     * 执行受保护的业务逻辑，通过Redisson分布式锁确保操作的互斥性。
     *
     * @param key      锁的标识符，用于在Redis中唯一确定一把锁。
     * @param waitTime 尝试获取锁的最大等待时间。
     * @param timeUnit 等待时间的单位。
     * @param supplier 无参函数，代表需要在锁保护下执行的业务逻辑，其返回值将透传给调用者。
     * @param <T>      业务逻辑返回值的类型。
     * @return 业务逻辑执行的结果。
     * @throws BusinessException 如果等待超时仍无法获取锁，则抛出业务异常。
     */
    @SneakyThrows
    public <T> T executeWithLock(String key, int waitTime, TimeUnit timeUnit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key); // 获取Redisson的锁实例。
        boolean acquired = lock.tryLock(waitTime, timeUnit); // 尝试获取锁。
        if (!acquired) {
            throw new BusinessException(CommonErrorEnum.LOCK_LIMIT); // 获取锁失败，抛出业务异常。
        }
        try {
            return supplier.get(); // 在锁保护下执行业务逻辑并返回结果。
        } finally {
            lock.unlock(); // 操作完成后释放锁，确保锁能被正确回收。
        }
    }

    /**
     * 重载方法，允许不指定等待时间，默认无限等待直到获取到锁。
     *
     * @param key      锁的标识符。
     * @param supplier 无参函数，业务逻辑提供者。
     * @param <T>      返回类型。
     * @return 业务执行结果。
     */
    public <T> T executeWithLock(String key, Supplier<T> supplier) {
        return executeWithLock(key, -1, TimeUnit.MILLISECONDS, supplier);
    }

    /**
     * 重载方法，专为不需要返回值的Runnable设计。
     *
     * @param key      锁的标识符。
     * @param runnable 需要在锁保护下执行的任务。
     */
    public void executeWithLock(String key, Runnable runnable) {
        executeWithLock(key, (Supplier<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 自定义Supplier接口，与Java.util.Function.Supplier类似，但允许抛出Throwable。
     *
     * @param <T> 返回类型。
     */
    @FunctionalInterface
    public interface Supplier<T> {
        T get() throws Throwable;
    }
}
