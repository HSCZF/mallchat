package com.hs.mallchat.common.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: CZF
 * @Create: 2024/6/28 - 9:16
 * Description: 申请状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyStatusEnum {

    WAIT_APPROVAL(1, "待审批"),

    AGREE(2, "同意");

    private final Integer code;

    private final String desc;
}

