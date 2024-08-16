package com.hs.mallchat.common.chat.service.strategy.mark;

import com.hs.mallchat.common.chat.domain.enums.MessageMarkTypeEnum;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 点赞标记策略类
 *
 * @Author: CZF
 * @Create: 2024/8/16 - 14:27
 */
@Component
public class LikeStrategy extends AbstractMsgMarkStrategy {
    @Override
    protected MessageMarkTypeEnum getTypeEnum() {
        return MessageMarkTypeEnum.LIKE;
    }

    @Override
    public void doMark(Long uid, Long msgId) {
        super.doMark(uid, msgId);
        //同时取消点踩的动作
        MsgMarkFactory.getStrategyNoNull(MessageMarkTypeEnum.DISLIKE.getType()).unMark(uid, msgId);
    }
}
