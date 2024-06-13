package com.hs.mallchat.common.common.utils;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 分布式锁-注解式的SEL表达式
 * SpEL工具类，提供方法用于解析Spring Expression Language表达式，
 * 通常用于动态获取方法参数值或执行复杂的表达式计算，支持在运行时解析方法签名和参数。
 *
 * @Author: CZF
 * @Date: 2024/6/13
 * @Time: 10:40 AM
 */
public class SpElUtils {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 构建方法的唯一标识键，格式为"类全限定名#方法名"。
     *
     * @param method 需要构建键的方法对象。
     * @return 方法的唯一标识键。
     */
    public static String getMethodKey(Method method) {
        return method.getDeclaringClass() + "#" + method.getName();
    }

    /**
     * 根据方法、方法参数以及SpEL表达式，解析表达式的值。
     * 支持在表达式中引用方法的参数，通过参数名访问其值。
     *
     * @param method 当前执行的方法。
     * @param args 方法的实际参数值数组。
     * @param spEl 需要解析的SpEL表达式字符串。
     * @return 表达式解析后的字符串结果。
     */
    public static String parseSpEl(Method method, Object[] args, String spEl) {
        // 获取方法参数名列表，如果无法获取则默认为空数组
        String[] parameterNames = Optional.ofNullable(PARAMETER_NAME_DISCOVERER.getParameterNames(method)).orElse(new String[]{});
        // 创建SpEL表达式上下文，用于存放变量信息
        EvaluationContext context = new StandardEvaluationContext();
        // 遍历参数名和参数值，将它们加入到上下文中
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        // 解析并编译SpEL表达式
        Expression expression = PARSER.parseExpression(spEl);
        // 在给定的上下文中计算表达式的值，并转换为String类型返回
        return expression.getValue(context, String.class);
    }
}
