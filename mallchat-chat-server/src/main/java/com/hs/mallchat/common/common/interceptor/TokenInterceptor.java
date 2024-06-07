package com.hs.mallchat.common.common.interceptor;

import com.hs.mallchat.common.common.exception.HttpErrorEnum;
import com.hs.mallchat.common.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * 登录拦截器，用于检查请求头中的令牌，验证用户登录状态。
 * 配置的拦截器不会直接生效，还需要配置InterceptorConfig类来配置拦截器。
 *
 * @Author: CZF
 * @Create: 2024/6/6 - 17:09
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    /**
     * 请求头中授权字段的名称
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 授权字段的前缀，例如 "Bearer token_value"
     * Bearer后接一个空格“ ”
     */
    public static final String AUTHORIZATION_SCHEMA = "Bearer ";

    /**
     * 用户ID的键名，用于在请求中存储有效用户ID
     */
    public static final String UID = "uid";

    /**
     * 常量布尔值，这里未使用
     */
    public static final boolean BOOLEAN = false;

    /**
     * 自动注入登录服务，用于验证令牌的有效性
     */
    @Autowired
    private LoginService loginService;

    /**
     * 拦截请求，检查用户登录状态。
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  将要处理请求的处理器
     * @return 如果用户已登录且请求不是公共接口，返回true继续处理；否则返回false，阻止处理
     * @throws Exception 可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request);
        Long validUid = loginService.getValidUid(token);
        if (Objects.nonNull(validUid)) { // 用户有登录态
            request.setAttribute(UID, validUid);
        } else { // 用户未登录，看看是不是公共接口
            boolean isPublicURL = isPublicURL(request);
            if (!isPublicURL) {
                // 401 错误处理，注释表示需要实现
                HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
                return false;
            }
        }
        return true;
    }

    /**
     * 判断请求URL是否为公共接口。
     *
     * @param request HTTP请求对象
     * @return 如果URL路径以"/public"开头，返回true，表示是公共接口；否则返回false
     */
    private boolean isPublicURL(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String[] split = requestURI.split("/");
        return split.length > 3 && "public".equals(split[3]);
    }

    /**
     * 从请求头中获取令牌。
     *
     * @param request HTTP请求对象
     * @return 如果请求头包含有效的授权字段，返回去除前缀后的令牌字符串；否则返回null
     */
    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        // 使用Optional对象处理可能为null的情况
        return Optional.ofNullable(header)
                .filter(h -> h.startsWith(AUTHORIZATION_SCHEMA))
                .map(h -> h.replaceFirst(AUTHORIZATION_SCHEMA, ""))
                .orElse(null);
    }

}
