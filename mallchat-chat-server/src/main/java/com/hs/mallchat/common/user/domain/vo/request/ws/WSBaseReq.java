package com.hs.mallchat.common.user.domain.vo.request.ws;

import com.hs.mallchat.common.user.domain.enums.WSReqTypeEnum;
import lombok.Data;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 18:16
 */
@Data
public class WSBaseReq {

    /**
     * @see WSReqTypeEnum
     */
    private Integer type;
    /**
     * 请求数据
     */
    private String data;

}
