package com.hs.mallchat.common.chat.domain.vo.request.admin;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Description:
 * 添加管理员请求信息
 *
 * @Author: CZF
 * @Create: 2024/8/29 - 20:36
 */
@Data
public class AdminAddReq {
    @NotNull
    @ApiModelProperty("房间号")
    private Long roomId;

    @NotNull
    @Size(min = 1, max = 3)
    @ApiModelProperty("需要添加管理的列表")
    private List<Long> uidList;
}
