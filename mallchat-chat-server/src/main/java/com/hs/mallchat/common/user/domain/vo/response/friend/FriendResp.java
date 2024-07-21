package com.hs.mallchat.common.user.domain.vo.response.friend;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: CZF
 * @Create: 2024/6/24 - 10:00
 * Description:
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendResp {

    @ApiModelProperty("好友uid")
    private Long uid;
    /**
     * @see com.hs.mallchat.common.user.domain.enums.ChatActiveStatusEnum
     */
    @ApiModelProperty("在线状态 1在线 2离线")
    private Integer activeStatus;


}
