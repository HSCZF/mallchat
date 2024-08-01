package com.hs.mallchat.common.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Description: 将扫码登录返回信息推送给所有横向扩展的服务
 *
 * @Author: CZF
 * @Create: 2024/7/31 - 9:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long uid;
    private Integer code;
}

