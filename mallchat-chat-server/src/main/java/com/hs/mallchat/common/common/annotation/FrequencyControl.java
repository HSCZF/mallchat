package com.hs.mallchat.common.common.annotation;

import java.lang.annotation.Repeatable;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * 频控注解，用于限制方法的访问频率。
 *
 * @Author: CZF
 * @Create: 2024/8/30 - 8:52
 */
// @Repeatable 元注解用于指定一个容器注解，使得可以在同一个声明位置多次使用同一个注解。
@Repeatable(FrequencyControlContainer.class)
public @interface FrequencyControl {

    /**
     * 频控目标枚举
     */
    enum Target {
        UID, IP, EL
    }

    /**
     * 频控key的前缀
     * key的前缀，默认取方法全限定名，除非在不同方法上对同一个资源做频控时自定义。
     *
     * @return 前缀键
     */
    String prefixKey() default "";

    /**
     * 频控对象，默认为 SpEL 表达式。
     * 对于 ip 和 uid 模式，需要是 http 入口的对象，保证RequestHolder里有值
     * ip=RequestHolder.get().getIp();
     * uid=RequestHolder.get().getUid().toString();
     *
     * @return 目标类型
     */
    Target target() default Target.EL;

    /**
     * springEl 表达式，当 target 为 EL 时必须填写。
     *
     * @return SpEL 表达式
     */
    String spEl() default "";

    /**
     * 频控时间范围，默认单位：秒
     *
     * @return 时间范围
     */
    int time();

    /**
     * 频控时间单位，默认：秒
     *
     * @return 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 频控次数
     * 单位时间内最大访问次数
     *
     * @return
     */
    int count();

}
