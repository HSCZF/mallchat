package com.hs.mallchat.common.common.exception;


import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Charsets;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.utils.JsonUtils;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.A;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 17:40
 */
@AllArgsConstructor
public enum HttpErrorEnum implements ErrorEnum{

    ACCESS_DENIED(401, "登录失效请重新登录");

    private Integer httpCode;
    private String msg;

    @Override
    public Integer getErrorCode() {
        return this.httpCode;
    }

    @Override
    public String getErrorMsg() {
        return this.msg;
    }

    public void sendHttpError(HttpServletResponse response) throws IOException {
        response.setStatus(this.getErrorCode());
        ApiResult responseData = ApiResult.fail(this);
        response.setContentType(ContentType.JSON.toString(Charsets.UTF_8));
        // response.getWriter().write(JsonUtils.toStr(ApiResult.fail(httpCode, msg)));
        response.getWriter().write(JSONUtil.toJsonStr(responseData));
    }

}
