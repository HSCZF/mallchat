package com.hs.mallchat.common.websocket.domain.vo.response;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 18:20
 * Description: 后端推送
 */
public class WSBaseResp<T> {
    /**
     * @see com.hs.mallchat.common.websocket.domain.enums.WSRespTypeEnum
     */
    private Integer type;
    private T data;
}
