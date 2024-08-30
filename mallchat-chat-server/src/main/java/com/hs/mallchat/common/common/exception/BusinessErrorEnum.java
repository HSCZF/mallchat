package com.hs.mallchat.common.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description:
 * 业务校验异常码
 * @Author: CZF
 * @Create: 2024/8/30 - 9:50
 */
@AllArgsConstructor
@Getter
public enum BusinessErrorEnum implements ErrorEnum{

    BUSINESS_ERROR(1001, "{0}"),
    SYSTEM_ERROR(1001, "系统出小差了，请稍后再试哦~~"),
    ;
    private Integer code;
    private String msg;

    @Override
    public Integer getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMsg() {
        return this.msg;
    }

}
