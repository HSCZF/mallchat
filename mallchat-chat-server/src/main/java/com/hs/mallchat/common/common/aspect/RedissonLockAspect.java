package com.hs.mallchat.common.common.aspect;

import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.service.LockService;
import com.hs.mallchat.common.common.utils.SpElUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 分布式锁-注解式的AOP切面
 * Redisson分布式锁切面处理类，用于自动应用分布式锁至标记有{@link RedissonLock}注解的方法上。
 * 利用AOP（面向切面编程）技术，在目标方法执行前后自动管理锁的获取与释放，简化并发控制逻辑。
 *
 * @Author: CZF
 * @Date: 2024/6/13
 * @Time: 10:31 AM
 */
@Component
@Aspect // 标识该类为切面类
@Order(0) // 定义切面的优先级，数字越小优先级越高，确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {

    @Autowired
    private LockService lockService; // 注入分布式锁服务，用于执行锁操作。

    /**
     * 环绕通知，用于拦截标记有{@link RedissonLock}注解的方法。
     * 在方法执行前后自动应用分布式锁，实现方法级别的并发控制。
     *
     * @param joinPoint   连接点，封装了目标方法的执行信息。
     * @param redissonLock 注解实例，包含锁的配置信息。
     * @return 目标方法执行后的返回值。
     * @throws Throwable 目标方法执行时可能抛出的异常。
     */
    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        // 获取当前执行方法的信息
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 构建锁的前缀，优先使用注解中定义的前缀，若为空则使用默认的基于方法签名构建的前缀
        String prefix = StringUtils.isBlank(redissonLock.prefixKey())
                       ? SpElUtils.getMethodKey(method)
                       : redissonLock.prefixKey();

        // 解析SpEL表达式，动态生成锁的键值，考虑方法参数的影响
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());

        // 组合完整的锁键并执行加锁后的业务逻辑
        return lockService.executeWithLock(prefix + ":" + key,
                                          redissonLock.waitTime(),
                                          redissonLock.unit(),
                                          joinPoint::proceed);
    }
}
