package com.hs.mallchat.common.common.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 17:54
 * Description: 拦截器
 * 每配置一个拦截器，就需要在这里加入进去执行
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Autowired
    private CollectorInterceptor collectorInterceptor;

    /**
     * 添加拦截器到拦截器注册表中并设定拦截顺序。
     * 本方法负责配置应用中的自定义拦截器链，确保按照指定逻辑对特定路径（/capi/**）的请求进行拦截处理。
     * 拦截器的添加顺序至关重要，因为后续拦截器可能依赖于前序拦截器的操作结果，
     * 如认证、日志记录或数据准备等。错误的顺序可能导致拦截逻辑失效或产生不可预期的行为。
     *
     * @param registry InterceptorRegistry，应用的拦截器管理系统，
     *                 用于注册拦截器及设置其作用的请求路径模式。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 首先添加token拦截器，此拦截器应优先执行，负责验证请求携带的token有效性，
        // 确保请求具有访问资源所需的授权。未通过此拦截将不会进入后续处理。
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/capi/**");

        // 其次添加collector拦截器，该拦截器收集请求相关的统计数据或执行其他非阻断性操作。
        // 基于前一个拦截器的成功执行，此拦截器可以进一步处理或记录信息。
        registry.addInterceptor(collectorInterceptor)
                .addPathPatterns("/capi/**");

        // 注意：拦截器执行顺序遵循注册顺序，更改顺序可能影响安全性和性能监控的准确性。
    }


}
