package com.hs.mallchat.common.common.service.frequencycontrol;

import com.hs.mallchat.common.common.domain.dto.FrequencyControlDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 限流策略工厂
 * 频率控制策略工厂类，用于管理和获取不同的频率控制策略
 *
 * @Author: CZF
 * @Create: 2024/8/30 - 9:21
 */
public class FrequencyControlStrategyFactory {

    // 私有构造方法，防止外部实例化
    private FrequencyControlStrategyFactory() {

    }

    /**
     * 指定时间内总次数限流的策略标识
     */
    public static final String TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER = "TotalCountWithInFixTime";

    /**
     * 限流策略集合，用于存储不同类型的频率控制服务
     * 初始化容量设置为8，避免扩容
     */
    static Map<String, AbstractFrequencyControlService<?>> frequencyControlServiceStrategyMap = new ConcurrentHashMap<>(8);


    /**
     * 注册频率控制器
     *
     * @param strategyName                    策略名称
     * @param abstractFrequencyControlService 频率控制服务实例
     * @param <K>                             频率控制数据传输对象的类型
     */
    public static <K extends FrequencyControlDTO> void registerFrequencyController(String strategyName, AbstractFrequencyControlService<K> abstractFrequencyControlService) {
        frequencyControlServiceStrategyMap.put(strategyName, abstractFrequencyControlService);
    }

    /**
     * 根据策略名称获取频率控制器
     *
     * @param strategyName 策略名称
     * @param <K>          频率控制数据传输对象的类型
     * @return 对应的频率控制服务实例
     */
    @SuppressWarnings("unchecked")
    public static <K extends FrequencyControlDTO> AbstractFrequencyControlService<K> getFrequencyControllerByName(String strategyName) {
        return (AbstractFrequencyControlService<K>) frequencyControlServiceStrategyMap.get(strategyName);
    }


}
