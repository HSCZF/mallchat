package com.hs.mallchat.common.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.hs.mallchat.common.common.annotation.FrequencyControl;
import com.hs.mallchat.common.common.domain.dto.FrequencyControlDTO;
import com.hs.mallchat.common.common.service.frequencycontrol.FrequencyControlStrategyFactory;
import com.hs.mallchat.common.common.service.frequencycontrol.FrequencyControlUtil;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.common.utils.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hs.mallchat.common.common.service.frequencycontrol.FrequencyControlStrategyFactory.TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;

/**
 * Description:
 * 频控实现，切面类
 *
 * @Author: CZF
 * @Create: 2024/8/30 - 8:59
 */

@Slf4j
@Aspect
@Component
public class FrequencyControlAspect {

    /**
     * 环绕通知，处理带有FrequencyControl或FrequencyControlContainer注解的方法
     *
     * @param joinPoint 切入点对象，包含被拦截方法的信息
     * @return 被拦截方法的执行结果
     * @throws Throwable 方法执行可能抛出的异常
     */
    @Around("@annotation(com.hs.mallchat.common.common.annotation.FrequencyControl)||@annotation(com.hs.mallchat.common.common.annotation.FrequencyControlContainer)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取被拦截方法的信息
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取方法上的FrequencyControl注解
        FrequencyControl[] annotationsByType = method.getAnnotationsByType(FrequencyControl.class);
        // 用于存储注解的前缀键和注解对象的映射
        Map<String, FrequencyControl> keyMap = new HashMap<>();
        for (int i = 0; i < annotationsByType.length; i++) {
            FrequencyControl frequencyControl = annotationsByType[i];
            // 构建注解的键，如果prefixKey为空，则使用方法名+注解在方法中的顺序作为前缀
            String prefix = StrUtil.isBlank(frequencyControl.prefixKey()) ? SpElUtils.getMethodKey(method) + ":index:" + i : frequencyControl.prefixKey();
            String key = "";
            // 根据注解的target属性确定具体的键值
            switch (frequencyControl.target()) {
                case EL:
                    // 使用SpEL表达式计算键值
                    key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), frequencyControl.spEl());
                    break;
                case IP:
                    // 使用请求的IP作为键值
                    key = RequestHolder.get().getIp();
                    break;
                case UID:
                    // 使用用户的UID作为键值
                    key = RequestHolder.get().getUid().toString();
            }
            // 将前缀键和具体键值映射到FrequencyControl对象
            keyMap.put(prefix + ":" + key, frequencyControl);
        }
        // 将注解的参数转换为编程式调用需要的参数
        List<FrequencyControlDTO> frequencyControlDtoList = keyMap.entrySet().stream()
                .map(entrySet -> buildFrequencyControlDTO(entrySet.getKey(), entrySet.getValue())).collect(Collectors.toList());
        // 调用编程式注解
        return FrequencyControlUtil.executeWithFrequencyControlList(TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER, frequencyControlDtoList, joinPoint::proceed);
    }

    /**
     * 构建FrequencyControlDTO对象，用于编程式调用
     *
     * @param key              限流的键
     * @param frequencyControl FrequencyControl注解对象
     * @return 构建的FrequencyControlDTO对象
     */
    private FrequencyControlDTO buildFrequencyControlDTO(String key, FrequencyControl frequencyControl) {
        FrequencyControlDTO frequencyControlDTO = new FrequencyControlDTO();
        frequencyControlDTO.setCount(frequencyControl.count());
        frequencyControlDTO.setTime(frequencyControl.time());
        frequencyControlDTO.setUnit(frequencyControl.unit());
        frequencyControlDTO.setKey(key);
        return frequencyControlDTO;
    }
}
