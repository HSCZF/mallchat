package com.hs.mallchat.common.user.domain.vo.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: CZF
 * @Create: 2024/6/18 - 11:40
 * Description:
 */
@Data
public class BlackReq {

    @ApiModelProperty("拉黑用户的uid")
    @NotNull
    private Long uid;

}
