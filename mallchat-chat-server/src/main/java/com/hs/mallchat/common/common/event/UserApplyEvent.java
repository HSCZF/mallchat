package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.user.domain.entity.UserApply;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * Description: 申请监听器事件
 *
 * @Author: CZF
 * @Create: 2024/8/2 - 16:31
 */
@Getter
public class UserApplyEvent extends ApplicationEvent {

    private UserApply userApply;

    public UserApplyEvent(Object source, UserApply userApply) {
        super(source);
        this.userApply = userApply;
    }
}
