package com.hs.mallchat.common.common.service.frequencycontrol;

import com.hs.mallchat.common.common.domain.dto.FrequencyControlDTO;
import com.hs.mallchat.common.common.utils.AssertUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

/**
 * Description:
 * 限流工具类 提供编程式的限流调用方法
 * 频率控制工具类，提供静态方法实现频率控制逻辑的封装
 *
 * @Author: CZF
 * @Create: 2024/8/30 - 9:18
 */
public class FrequencyControlUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private FrequencyControlUtil() {

    }

    /**
     * 根据策略名称和一系列频率控制对象执行频率控制，并调用相应的 Supplier
     *
     * @param strategyName         限流策略名称
     * @param frequencyControlList 限流控制列表
     * @param supplier             供供应商函数，当不限流时执行
     * @param <T>                  返回类型泛型
     * @param <K>                  限流控制DTO泛型，需继承FrequencyControlDTO
     * @return Supplier执行结果
     * @throws Throwable 执行过程中抛出的异常
     */
    public static <T, K extends FrequencyControlDTO> T executeWithFrequencyControlList(String strategyName,
                                                                                       List<K> frequencyControlList,
                                                                                       AbstractFrequencyControlService.SupplierThrowWithoutParam<T> supplier) throws Throwable {
        // 检查是否存在限流策略Key为空的情况
        boolean existsFrequencyControlHasNullKey = frequencyControlList.stream()
                .anyMatch(frequencyControl -> ObjectUtils.isEmpty(frequencyControl.getKey()));
        // 断言不存在空的限流策略Key
        AssertUtil.isFalse(existsFrequencyControlHasNullKey, "限流策略的Key字段不允许出现空值");
        // 根据策略名称获取限流控制器
        AbstractFrequencyControlService<K> frequencyController = FrequencyControlStrategyFactory.getFrequencyControllerByName(strategyName);
        // 使用限流控制器执行限流逻辑并返回结果
        return frequencyController.executeWithFrequencyControlList(frequencyControlList, supplier);
    }


}
