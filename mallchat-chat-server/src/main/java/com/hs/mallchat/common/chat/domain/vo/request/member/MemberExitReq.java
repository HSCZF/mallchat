package com.hs.mallchat.common.chat.domain.vo.request.member;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Description:
 * 退出群聊
 *
 * @Author: CZF
 * @Create: 2024/8/23 - 16:22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberExitReq {
    @NotNull
    @ApiModelProperty("房间id")
    private Long roomId;
}

