package com.hs.mallchat.common.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 * 这个注解的作用是允许在一个方法上多次使用 FrequencyControl 注解，并将它们以数组的形式存储起来。
 *
 * @Author: CZF
 * @Create: 2024/8/30 - 8:52
 */
@Retention(RetentionPolicy.RUNTIME)//运行时生效
@Target(ElementType.METHOD)//作用在方法上
public @interface FrequencyControlContainer {
    FrequencyControl[] value();

}
