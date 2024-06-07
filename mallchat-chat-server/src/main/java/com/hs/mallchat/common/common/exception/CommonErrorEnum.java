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

    BUSINESS_ERROR(0, "{0}"),
    SYSTEM_ERROR(-1, "系统出小差了，请稍后再试哦~~"),
    PARAM_INVALID(-2, "参数校验失败"),
    LOCK_LIMIT(-3, "请求太频繁了，请稍后再试"),
    ;

    private final Integer code;
    private final String msg;


    // 使用接口规范，使用枚举实现
    /**
     * 在ApiResult那直接获取错误码和错误信息
     * return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR.getCode());
     * return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR);
     * 直接变成
     *
     * @return
     */

    @Override
    public Integer getErrorCode() {
        return code;
    }

    @Override
    public String getErrorMsg() {
        return msg;
    }
}
