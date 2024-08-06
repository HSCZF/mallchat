package com.hs.mallchat.common.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 消息撤回的推送类
 *
 * @Author: CZF
 * @Create: 2024/8/6 - 10:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMsgRecallDTO {
    private Long msgId;
    private Long roomId;
    //撤回的用户
    private Long recallUid;
}
