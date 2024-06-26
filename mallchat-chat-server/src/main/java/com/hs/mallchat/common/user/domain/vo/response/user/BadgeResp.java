package com.hs.mallchat.common.user.domain.vo.response.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: CZF
 * @Create: 2024/6/9 - 15:10
 * Description:
 */
@Data
public class BadgeResp {

    @ApiModelProperty(value = "徽章id")
    private Long id;

    @ApiModelProperty(value = "徽章图标")
    private String img;

    @ApiModelProperty(value = "徽章描述")
    private String describe;

    @ApiModelProperty(value = "是否拥有 0否 1是")
    private Integer obtain;

    @ApiModelProperty(value = "是否佩戴 0否 1是")
    private Integer wearing;

}
