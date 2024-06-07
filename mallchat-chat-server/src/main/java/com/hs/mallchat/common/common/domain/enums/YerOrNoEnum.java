package com.hs.mallchat.common.common.domain.enums;

import lombok.*;

/**
 * @Author: CZF
 * @Create: 2024/6/7 - 11:02
 * Description:
 */
@Getter
@AllArgsConstructor
public enum YerOrNoEnum {

    NO(0, "否"),
    YES(1, "是"),
    ;

    private final Integer status;
    private final String desc;

}
