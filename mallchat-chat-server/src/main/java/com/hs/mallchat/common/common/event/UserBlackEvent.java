package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 拉黑用户
 * @author CZF
 */
@Getter
public class UserBlackEvent extends ApplicationEvent {

    /**
     * 注册成功的用户信息。
     */
    private User user;

    /**
     * 构造函数，初始化用户注册事件。
     *
     * @param source 事件的来源，通常为发布事件的bean。
     * @param user 注册成功的用户信息。
     */
    public UserBlackEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
