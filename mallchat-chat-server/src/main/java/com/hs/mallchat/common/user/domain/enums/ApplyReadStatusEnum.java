package com.hs.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: CZF
 * @Create: 2024/6/27 - 16:44
 * Description: 申请阅读状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyReadStatusEnum {

    UNREAD(1, "未读"),

    READ(2, "已读");

    private final Integer code;

    private final String desc;
}