package com.hs.mallchat.transaction.aspect;

import cn.hutool.core.date.DateUtil;
import com.hs.mallchat.transaction.annotation.SecureInvoke;
import com.hs.mallchat.transaction.domain.dto.SecureInvokeDTO;
import com.hs.mallchat.transaction.domain.entity.SecureInvokeRecord;
import com.hs.mallchat.transaction.service.SecureInvokeHolder;
import com.hs.mallchat.transaction.service.SecureInvokeService;
import com.hs.mallchat.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description: 安全执行切面
 * AOP切面类用于安全调用机制，确保方法在事务内执行或重试。
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 15:51
 */
@Slf4j
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // 确保最先执行
@Component
public class SecureInvokeAspect {

    /**
     * 注入安全调用服务，用于处理安全调用逻辑。
     */
    @Autowired
    private SecureInvokeService secureInvokeService;

    /**
     * 环绕通知，拦截带有@SecureInvoke注解的方法调用。
     *
     * @param joinPoint    切入点对象，包含目标方法的信息及参数。
     * @param secureInvoke SecureInvoke注解实例，包含方法级别的安全调用配置。
     * @return 目标方法的返回结果，或null（当异步执行时）。
     * @throws Throwable 可能抛出的异常，包括目标方法抛出的异常。
     */
    @Around("@annotation(secureInvoke)")
    public Object around(ProceedingJoinPoint joinPoint, SecureInvoke secureInvoke) throws Throwable {
        // 判断是否为异步调用及当前是否处于事务中。
        boolean async = secureInvoke.async();
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();

        // 如果已经在安全调用过程中或不在事务中，则直接执行目标方法。
        if (SecureInvokeHolder.isInvoking() || !inTransaction) {
            return joinPoint.proceed();
        }

        // 获取目标方法的元信息。
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 构建参数类型列表的字符串表示。
        List<String> parameters = Stream.of(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.toList());

        // 创建安全调用数据传输对象。
        SecureInvokeDTO dto = SecureInvokeDTO.builder()
                .args(JsonUtils.toStr(joinPoint.getArgs()))
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(JsonUtils.toStr(parameters))
                .build();

        // 创建安全调用记录，包含重试次数和下次重试时间。
        SecureInvokeRecord record = SecureInvokeRecord.builder()
                .secureInvokeDTO(dto)
                .maxRetryTimes(secureInvoke.maxRetryTimes())
                .nextRetryTime(DateUtil.offsetMinute(new Date(), (int) SecureInvokeService.RETRY_INTERVAL_MINUTES))
                .build();

        // 将安全调用记录传递给安全调用服务，根据配置决定是否异步执行。
        secureInvokeService.invoke1(record, async);

        // 异步执行时返回null，否则返回目标方法的结果。
        return async ? null : joinPoint.proceed();
    }
}
