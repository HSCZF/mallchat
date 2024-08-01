package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/30 - 16:44
 */
@Getter
public class UserOfflineEvent extends ApplicationEvent {

    private final User user;

    public UserOfflineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
