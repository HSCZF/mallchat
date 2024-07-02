package com.hs.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: CZF
 * @Create: 2024/6/27 - 16:32
 * Description: 申请类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyTypeEnum {

    ADD_FRIEND(1, "加好友"),
    ;

    private final Integer code;
    private final String desc;

}
