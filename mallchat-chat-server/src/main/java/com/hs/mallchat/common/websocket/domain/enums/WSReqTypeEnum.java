package com.hs.mallchat.common.websocket.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum WSReqTypeEnum {
    LOGIN(1, "请求登录二维码"),
    HEARTBEAT(2, "心跳包"),
    AUTHORIZE(3, "登录认证"),
    ;

    private final Integer type;
    private final String desc;

    private static Map<Integer, WSReqTypeEnum> cache;

    /**
     * 静态初始化块，用于在类加载时初始化请求类型缓存。
     * 这个缓存映射是为了快速查找和映射WSReqTypeEnum中的请求类型到它们对应的值。
     * 使用Arrays.stream和Collectors.toMap函数从WSReqTypeEnum的枚举常量中构建一个Map，
     * 其中键是请求类型的值，值是请求类型的实例本身。
     * 这样可以在后续操作中通过请求类型的值快速获取对应的请求类型实例，提高了效率。
     */
    static {
        cache = Arrays.stream(WSReqTypeEnum.values())
                .collect(Collectors.toMap(WSReqTypeEnum::getType, Function.identity()));
    }


    /**
     * 根据类型代码获取对应的WSReqTypeEnum实例。
     *
     * 本方法通过一个整型代码从缓存中检索对应的WSReqTypeEnum实例。这种方法的设计旨在快速访问常量信息，
     * 减少了通过传统查找方式带来的性能开销。缓存机制确保了类型代码到实例的映射是一次性构建并重复使用的，
     * 从而优化了应用程序的性能。
     *
     * @param type 类型代码，对应WSReqTypeEnum中的枚举值。
     * @return 对应于给定类型代码的WSReqTypeEnum实例。如果找不到匹配的实例，则返回null。
     */
    public static WSReqTypeEnum of(Integer type) {
        return cache.get(type);
    }
}
