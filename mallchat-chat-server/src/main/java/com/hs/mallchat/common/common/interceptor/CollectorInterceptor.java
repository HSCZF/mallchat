package com.hs.mallchat.common.common.interceptor;

import cn.hutool.extra.servlet.ServletUtil;
import com.hs.mallchat.common.common.domain.dto.RequestInfo;
import com.hs.mallchat.common.common.utils.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @Author: CZF
 * @Create: 2024/6/7 - 10:04
 * Description: 信息收集的拦截器
 * 配置的拦截器不会直接生效，还需要配置InterceptorConfig类来配置拦截器。
 */
@Order(1)
@Slf4j
@Component
public class CollectorInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前进行拦截。主要功能是获取用户ID和客户端IP，
     * 并将这些信息包装成RequestInfo对象，存储在RequestHolder中，
     * 以供后续环节使用。
     *
     * @param request  HTTP请求对象，从中获取用户ID和客户端IP。
     * @param response HTTP响应对象，当前方法不直接操作响应内容。
     * @param handler  将要处理请求的目标对象，可以是控制器或其他类型的处理器。
     * @return 始终返回true，表示无论条件如何，都会继续处理请求。
     * @throws Exception 如果在处理过程中发生异常，会抛出此异常。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 创建RequestInfo对象，用于存储请求的相关信息。
        RequestInfo info = new RequestInfo();
        // 尝试从请求中获取用户ID，首先通过getAttribute获取可能的UID字符串，
        // 然后转换为Long类型。如果获取不到，则默认为null。
        Long uid = Optional.ofNullable(request.getAttribute(TokenInterceptor.ATTRIBUTE_UID))
                .map(Object::toString)
                .map(Long::parseLong)
                .orElse(null);
        // 设置用户ID和客户端IP到RequestInfo对象中。
        info.setUid(uid);
        info.setIp(ServletUtil.getClientIP(request));
        // 将RequestInfo对象设置到RequestHolder中，以供后续使用。
        RequestHolder.set(info);
        // 返回true，表示无论条件如何，都会继续处理请求。
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestHolder.remove();
    }
}
