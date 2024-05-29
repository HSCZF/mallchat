package com.hs.mallchat.common.websocket.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: CZF
 * @Create: 2024/5/29 - 16:42
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSChannelExtraDTO {

    /**
     * 前端如果登录了，记录uid
     */
    private Long uid;

}
