package com.hs.mallchat.common.user.domain.vo.response.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 表情包反参
 *
 * @Author: CZF
 * @Create: 2024/8/8 - 10:12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEmojiResp {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 表情地址
     */
    @ApiModelProperty(value = "表情url")
    private String expressionUrl;

}
