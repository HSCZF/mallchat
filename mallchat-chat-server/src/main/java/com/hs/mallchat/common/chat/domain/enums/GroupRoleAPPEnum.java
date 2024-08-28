package com.hs.mallchat.common.chat.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * 热点枚举
 * @Author: CZF
 * @Create: 2024/8/21 - 21:07
 */
@AllArgsConstructor
@Getter
public enum GroupRoleAPPEnum {
    LEADER(1, "群主"),
    MANAGER(2, "管理"),
    MEMBER(3, "普通成员"),
    REMOVE(4, "被移除的成员"),
    ;

    private final Integer type;
    private final String desc;

    private static Map<Integer, GroupRoleAPPEnum> cache;

    static {
        cache = Arrays.stream(GroupRoleAPPEnum.values()).collect(Collectors.toMap(GroupRoleAPPEnum::getType, Function.identity()));
    }

    public static GroupRoleAPPEnum of(Integer type) {
        return cache.get(type);
    }
}