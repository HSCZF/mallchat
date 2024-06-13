package com.hs.mallchat.common.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解，用于在方法级别便捷地应用分布式锁逻辑。
 * 该注解配合AOP（面向切面编程）使用，可以自动处理加锁和解锁过程，
 * 使得业务方法在执行时能保证资源访问的互斥性，防止并发问题。
 *
 * @Author: CZF
 * @Date: 2024/6/13
 * @Time: 10:22 AM
 * Description: 分布式锁-注解式
 */
@Retention(RetentionPolicy.RUNTIME) // 注解保留至运行时，可通过反射访问
@Target(ElementType.METHOD) // 该注解只能应用于方法上
public @interface RedissonLock {

    /**
     * 锁的前缀关键字，默认取方法全限定名，可以自己指定
     * @return 锁的前缀字符串，默认为空字符串。
     */
    String prefixKey() default "";

    /**
     * 锁的键名部分，与`prefixKey`结合构成完整的Redis锁键。
     * 支持springEl表达式，的key
     * 键名需要具有唯一性，以确保正确锁定对应的资源。
     *
     * @return 锁的键名字符串。
     */
    String key();

    /**
     * 等待锁的排队时间，默认快速失败
     * @return 等待时间（单位取决于`unit`），默认为-1。
     */
    int waitTime() default -1;

    /**
     * 等待时间的单位。
     * 当`waitTime`非负时，此单位生效，用于指定等待时间的具体度量。
     *
     * @return 时间单位，默认为毫秒（TimeUnit.MILLISECONDS）。
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

}
