package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件类，继承自Spring的ApplicationEvent。
 * 用于在用户注册完成后，通知系统中的其他监听器用户注册的信息。
 */
@Getter
public class UserRegisterEvent extends ApplicationEvent {

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
    public UserRegisterEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
