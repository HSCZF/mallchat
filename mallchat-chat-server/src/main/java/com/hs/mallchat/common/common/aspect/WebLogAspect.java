package com.hs.mallchat.common.common.aspect;

import cn.hutool.core.date.StopWatch;
import cn.hutool.json.JSONUtil;
import com.hs.mallchat.common.common.domain.dto.RequestInfo;
import com.hs.mallchat.common.common.utils.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description:
 * 日志切面
 *
 * @Author: CZF
 * @Create: 2024/9/2 - 20:09
 */
@Aspect
@Slf4j
@Component
public class WebLogAspect {

    /**
     * 环绕通知，用于拦截控制器方法的执行
     * 接收到请求，记录请求内容
     * 所有controller包下所有的类的方法，都是切点
     * <p>
     * 如果ApiResult返回success=false，则打印warn日志；
     * warn日志只能打印在同一行，因为只有等到ApiResult结果才知道是success=false。
     * <p>
     * 如果ApiResult返回success=true，则打印info日志；
     * 特别注意：由于info级别日志已经包含了warn级别日志。如果开了info级别日志，warn就不会打印了。
     *
     * @param joinPoint 连接点对象，包含ProceedingJoinPoint接口，可以控制方法的执行
     * @return 方法执行的结果对象
     * @throws Throwable 方法执行中可能抛出的异常
     */
    @Around("execution(* com..controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取HttpServletRequest对象
        HttpServletRequest request = ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        // 获取HTTP请求方法
        String method = request.getMethod();
        // 获取请求的URI
        String uri = request.getRequestURI();
        // 过滤掉HttpRequest和HttpResponse参数，避免打印这些对象
        List<Object> paramList = Stream.of(joinPoint.getArgs())
                .filter(args -> !(args instanceof ServletRequest))
                .filter(args -> !(args instanceof ServletResponse))
                .collect(Collectors.toList());
        // 将参数列表转换为JSON字符串以便打印
        String printParamStr = paramList.size() == 1 ? JSONUtil.toJsonStr(paramList.get(0)) : JSONUtil.toJsonStr(paramList);
        // 获取请求信息，包括用户信息等
        RequestInfo requestInfo = RequestHolder.get();
        // 将请求信息转换为JSON字符串以便打印
        String userHeaderStr = JSONUtil.toJsonStr(requestInfo);
        // 如果启用了info日志级别，则打印请求信息
        if (log.isInfoEnabled()) {
            log.info("[{}][{}]【base:{}】【request:{}】", method, uri, userHeaderStr, printParamStr);
        }
        // 计时器，用于计算方法执行时间
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 执行连接点的方法
        Object result = joinPoint.proceed();
        stopWatch.stop();
        // 获取方法执行时间
        long cost = stopWatch.getTotalTimeMillis();
        // 将方法执行结果转换为JSON字符串以便打印
        String printResultStr = JSONUtil.toJsonStr(result);
        // 如果启用了info日志级别，则打印响应信息和执行时间
        if (log.isInfoEnabled()) {
            log.info("[{}]【response:{}】[cost:{}ms]", uri, printResultStr, cost);
        }
        // 返回方法执行结果
        return result;
    }

}
