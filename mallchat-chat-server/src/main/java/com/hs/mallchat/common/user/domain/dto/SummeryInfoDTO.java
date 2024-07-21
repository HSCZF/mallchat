package com.hs.mallchat.common.user.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户摘要信息数据传输对象。
 * 用于在接口返回中承载用户的简要信息，包括用户ID、是否需要刷新、用户昵称、用户头像、归属地等。
 *
 * @Author: CZF
 * @Create: 2024/7/2 - 10:58
 * Description: 用户聚合信息-返回的代表需要刷新的
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SummeryInfoDTO {
    @ApiModelProperty(value = "用户id")
    private Long uid;
    @ApiModelProperty(value = "是否需要刷新")
    private Boolean needRefresh = Boolean.TRUE;
    @ApiModelProperty(value = "用户昵称")
    private String name;
    @ApiModelProperty(value = "用户头像")
    private String avatar;
    @ApiModelProperty(value = "归属地")
    private String locPlace;
    @ApiModelProperty("佩戴的徽章id")
    private Long wearingItemId;
    @ApiModelProperty(value = "用户拥有的徽章id列表")
    List<Long> itemIds;

    /**
     * 创建一个不需要刷新信息的用户摘要对象。
     * 用于在用户信息未发生变化时，标记为不需要刷新。
     *
     * @param uid 用户ID
     * @return SummeryInfoDTO 不需要刷新的用户摘要信息对象
     */
    public static SummeryInfoDTO skip(Long uid) {
        SummeryInfoDTO dto = new SummeryInfoDTO();
        dto.setUid(uid);
        dto.setNeedRefresh(Boolean.FALSE);
        return dto;
    }
}
