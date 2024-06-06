package com.hs.mallchat.common.common.domain.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 10:15
 * Description: 通用返回体
 */
@Data
@ApiModel("基础返回体")
public class ApiResult<T> {

    @ApiModelProperty("成功标识true or false")
    private Boolean success;

    @ApiModelProperty("错误码")
    private Integer errCode;

    @ApiModelProperty("错误消息")
    private String errMsg;

    @ApiModelProperty("返回对象")
    private T data;

    /**
     * 创建一个表示成功结果的ApiResult对象。
     * @param <T> 结果数据的泛型类型。
     * @return 一个包含成功标记和可能的数据的ApiResult对象。
     */
    public static <T> ApiResult<T> success() {
        ApiResult<T> result = new ApiResult<T>();
        result.setData(null);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    /**
     * 创建一个包含指定数据的成功结果的ApiResult对象。
     * @param <T> 结果数据的泛型类型。
     * @param data 成功结果中的数据。
     * @return 一个包含成功标记和数据的ApiResult对象。
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<T>();
        result.setData(data);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    /**
     * 创建一个表示失败结果的ApiResult对象。
     * @param <T> 结果数据的泛型类型。
     * @param code 错误代码。
     * @param msg 错误信息。
     * @return 一个包含失败标记、错误代码和错误信息的ApiResult对象。
     */
    public static <T> ApiResult<T> fail(Integer code, String msg) {
        ApiResult<T> result = new ApiResult<T>();
        result.setSuccess(Boolean.FALSE);
        result.setErrCode(code);
        result.setErrMsg(msg);
        return result;
    }

    /**
     * 检查当前ApiResult对象是否表示成功。
     * @return 如果成功则返回true，否则返回false。
     */
    public boolean isSuccess() {
        return this.success;
    }

}
