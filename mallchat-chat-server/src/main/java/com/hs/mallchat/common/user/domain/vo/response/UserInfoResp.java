package com.hs.mallchat.common.user.domain.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 9:27
 */
@Data
public class UserInfoResp {

    /**
     * 用户的唯一标识
     */
    @ApiModelProperty(value = "uid")
    private Long id;

    /**
     * 用户的姓名
     */
    @ApiModelProperty(value = "用户名称", required = true)
    private String name;

    /**
     * 用户的头像链接
     */
    @ApiModelProperty(value = "用户头像")
    private String avatar;

    /**
     * 用户的性别，1表示男性，2表示女性
     */
    @ApiModelProperty(value = "用户性别，1表示男性，2表示女性")
    private Integer sex;

    /**
     * 用户名称修改次数限制
     */
    @ApiModelProperty(value = "剩余的改名次数")
    private Integer modifyNameChance;

}
