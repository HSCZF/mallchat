package com.hs.mallchat.common.chat.service.strategy.mark;

import com.hs.mallchat.common.common.exception.CommonErrorEnum;
import com.hs.mallchat.common.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * 消息标记策略工厂
 * <p>
 * 该类用于管理和提供不同消息标记策略的单例
 *
 * @Author: CZF
 * @Create: 2024/8/16 - 14:27
 */

public class MsgMarkFactory {

    /**
     * 策略映射表，用于存储消息标记类型及其对应的策略实现
     */
    private static final Map<Integer, AbstractMsgMarkStrategy> STRATEGY_MAP = new HashMap<>();

    /**
     * 注册消息标记策略
     * 将指定类型的消息标记策略关联到工厂，以便后续使用
     *
     * @param markType 消息标记类型
     * @param strategy 消息标记策略实现
     */
    public static void register(Integer markType, AbstractMsgMarkStrategy strategy) {
        STRATEGY_MAP.put(markType, strategy);
    }

    /**
     * 根据消息标记类型获取对应的策略，如果找不到或策略为空，则抛出异常
     *
     * @param markType 消息标记类型
     * @return 对应的消息标记策略实例，不会返回null
     * @throws IllegalArgumentException 如果找不到对应策略或策略为空时，抛出该异常
     */
    public static AbstractMsgMarkStrategy getStrategyNoNull(Integer markType) {
        AbstractMsgMarkStrategy strategy = STRATEGY_MAP.get(markType);
        // 确保获取的策略不为空，否则抛出参数无效的异常
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_INVALID);
        return strategy;
    }

}
