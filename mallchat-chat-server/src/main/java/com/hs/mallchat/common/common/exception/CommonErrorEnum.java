package com.hs.mallchat.common.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: CZF
 * @Create: 2024/6/7 - 15:41
 * Description:
 */
@Getter
@AllArgsConstructor
public enum CommonErrorEnum implements ErrorEnum{

    // BUSINESS_ERROR(0, "{0}"), //这个直接在PARAM_INVALID里面加上，直接携带参数信息
    SYSTEM_ERROR(-1, "系统出小差了，请稍后再试哦~~"),
    PARAM_INVALID(-2, "参数校验失败{0}"),
    FREQUENCY_LIMIT(-3, "请求太频繁了，请稍后再试哦~~"),
    LOCK_LIMIT(-4, "请求太频繁了，请稍后再试"),
    ;

    private final Integer code;
    private final String msg;

    @Override
    public Integer getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMsg() {
        return this.msg;
    }
}
