package com.hs.mallchat.common.common.domain.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 本地消息表id
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 20:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdRespVO {
    @ApiModelProperty("id")
    private long id;

    public static IdRespVO id(Long id) {
        IdRespVO idRespVO = new IdRespVO();
        idRespVO.setId(id);
        return idRespVO;
    }
}