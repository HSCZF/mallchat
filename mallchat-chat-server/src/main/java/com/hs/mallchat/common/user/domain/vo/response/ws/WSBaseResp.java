package com.hs.mallchat.common.user.domain.vo.response.ws;

import com.hs.mallchat.common.user.domain.enums.WSRespTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 18:20
 * Description: ws的基本返回信息体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSBaseResp<T> {
    /**
     * ws推送给前端的消息
     * @see WSRespTypeEnum
     */
    private Integer type;
    private T data;
}
