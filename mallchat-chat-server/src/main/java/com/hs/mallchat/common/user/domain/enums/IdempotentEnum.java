package com.hs.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: CZF
 * @Create: 2024/6/13 - 9:17
 * Description:
 */
@Getter
@AllArgsConstructor
public enum IdempotentEnum {

    UID(1, "uid"),
    MSG_ID(2, "消息id"),
    ;

    private final Integer type;
    private final String desc;

}
