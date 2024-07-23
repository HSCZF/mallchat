package com.hs.mallchat.common.chat.service.strategy.msg;

import com.hs.mallchat.common.common.exception.CommonErrorEnum;
import com.hs.mallchat.common.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 21:01
 */
public class MsgHandlerFactory {

    private static final Map<Integer, AbstractMsgHandler> STRATEGY_MAP = new HashMap<>();

    public static void register(Integer code, AbstractMsgHandler strategy) {
        STRATEGY_MAP.put(code, strategy);
    }

    public static AbstractMsgHandler getStrategyNoNull(Integer code) {
        AbstractMsgHandler strategy = STRATEGY_MAP.get(code);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_INVALID);
        return strategy;
    }


}
