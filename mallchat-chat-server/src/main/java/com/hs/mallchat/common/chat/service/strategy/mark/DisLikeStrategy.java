package com.hs.mallchat.common.chat.service.strategy.mark;

import com.hs.mallchat.common.chat.domain.enums.MessageMarkTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 点踩标记策略类
 *
 * @Author: CZF
 * @Create: 2024/8/16 - 14:27
 */
@Component
public class DisLikeStrategy extends AbstractMsgMarkStrategy {

    @Override
    protected MessageMarkTypeEnum getTypeEnum() {
        return MessageMarkTypeEnum.DISLIKE;
    }

    @Override
    protected void doMark(Long uid, Long msgId) {
        super.doMark(uid, msgId);
        //同时取消点赞的动作
        MsgMarkFactory.getStrategyNoNull(MessageMarkTypeEnum.LIKE.getType()).unMark(uid, msgId);
    }
}
