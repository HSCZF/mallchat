package com.hs.mallchat.common.websocket.domain.vo.request;

import lombok.Data;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 18:16
 */
@Data
public class WSBaseReq {

    /**
     * @see com.hs.mallchat.common.websocket.domain.enums.WSReqTypeEnum
     */
    private Integer type;
    private String data;

}
