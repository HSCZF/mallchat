package com.hs.mallchat.common.user.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: CZF
 * @Create: 2024/6/9 - 18:10
 * Description:
 */
@Data
public class WearingBadgeReq {

    @ApiModelProperty("徽章id")
    @NotNull
    private Long itemId;

}
