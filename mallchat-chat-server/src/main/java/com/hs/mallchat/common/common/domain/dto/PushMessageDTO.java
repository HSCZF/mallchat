package com.hs.mallchat.common.common.domain.dto;

import com.hs.mallchat.common.user.domain.enums.WSPushTypeEnum;
import com.hs.mallchat.common.user.domain.vo.response.ws.WSBaseResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Description: 推送给用户的消息对象
 *
 * @Author: CZF
 * @Create: 2024/7/29 - 16:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessageDTO {

    /**
     * 推送的ws消息
     */
    private WSBaseResp<?> wsBaseMsg;
    /**
     * 推送的uid
     */
    private List<Long> uidList;

    /**
     * 推送类型 1个人 2全员
     *
     * @see com.hs.mallchat.common.user.domain.enums.WSPushTypeEnum
     */
    private Integer pushType;

    public PushMessageDTO(Long uid, WSBaseResp<?> wsBaseMsg) {
        this.uidList = Collections.singletonList(uid);
        this.wsBaseMsg = wsBaseMsg;
        this.pushType = WSPushTypeEnum.USER.getType();
    }

    public PushMessageDTO(List<Long> uidList, WSBaseResp<?> wsBaseMsg) {
        this.uidList = uidList;
        this.wsBaseMsg = wsBaseMsg;
        this.pushType = WSPushTypeEnum.USER.getType();
    }

    public PushMessageDTO(WSBaseResp<?> wsBaseMsg) {
        this.wsBaseMsg = wsBaseMsg;
        this.pushType = WSPushTypeEnum.ALL.getType();
    }


}
