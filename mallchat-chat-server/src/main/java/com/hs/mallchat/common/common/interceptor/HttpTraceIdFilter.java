package com.hs.mallchat.common.common.interceptor;

import com.hs.mallchat.common.common.constant.MDCKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.UUID;

/**
 * Description:
 * 设置链路追踪的值，初期单体项目先简单用
 * 在logback.xml中格式化加入%X{tid}以便追踪
 *
 * @Author: CZF
 * @Create: 2024/9/2 - 21:06
 */
@Slf4j
@WebFilter(urlPatterns = "/*")
public class HttpTraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String tid = UUID.randomUUID().toString();
        MDC.put(MDCKey.TID, tid);
        chain.doFilter(request, response);
        MDC.remove(MDCKey.TID);
    }
}
