package com.hs.mallchat.common.chat.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Description:
 * 新建群组
 * @Author: CZF
 * @Create: 2024/8/23 - 16:25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupAddReq {
    @NotNull
    @Size(min = 1, max = 50)
    @ApiModelProperty("邀请的uid")
    private List<Long> uidList;
}

