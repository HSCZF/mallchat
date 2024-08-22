package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.user.domain.entity.UserBackpack;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/22 - 20:51
 */
@Getter
public class ItemReceiveEvent extends ApplicationEvent {
    private UserBackpack userBackpack;

    public ItemReceiveEvent(Object source, UserBackpack userBackpack) {
        super(source);
        this.userBackpack = userBackpack;
    }

}
