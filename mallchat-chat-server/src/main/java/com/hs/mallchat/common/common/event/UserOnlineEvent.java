package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author: CZF
 * @Create: 2024/6/17 - 11:27
 * Description:
 */
@Getter
public class UserOnlineEvent extends ApplicationEvent {

    private User user;
    public UserOnlineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
