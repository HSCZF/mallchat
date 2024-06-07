package com.hs.mallchat.common.user.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author: CZF
 * @Create: 2024/6/7 - 11:19
 * Description:
 */
@Data
public class ModifyNameByReq {

    @ApiModelProperty("用户名")
    @NotBlank
    @Length(max = 6, message = "用户名不可以取太长，不然我记不住噢")
    private String name;

}
