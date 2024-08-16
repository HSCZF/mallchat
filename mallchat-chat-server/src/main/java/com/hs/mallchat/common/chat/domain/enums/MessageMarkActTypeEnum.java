package com.hs.mallchat.common.chat.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * 消息标记动作类型
 * @Author: CZF
 * @Create: 2024/8/16 - 14:31
 */
@AllArgsConstructor
@Getter
public enum MessageMarkActTypeEnum {
    MARK(1, "确认标记"),
    UN_MARK(2, "取消标记"),
    ;

    private final Integer type;
    private final String desc;

    private static Map<Integer, MessageMarkActTypeEnum> cache;

    static {
        cache = Arrays.stream(MessageMarkActTypeEnum.values()).collect(Collectors.toMap(MessageMarkActTypeEnum::getType, Function.identity()));
    }

    public static MessageMarkActTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
