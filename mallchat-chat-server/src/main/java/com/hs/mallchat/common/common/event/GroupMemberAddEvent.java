package com.hs.mallchat.common.common.event;

import com.hs.mallchat.common.chat.domain.entity.GroupMember;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.List;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/23 - 16:30
 */
@Getter
public class GroupMemberAddEvent extends ApplicationEvent {

    private final List<GroupMember> memberList;
    private final RoomGroup roomGroup;
    private final Long inviteUid;

    public GroupMemberAddEvent(Object source, RoomGroup roomGroup, List<GroupMember> memberList, Long inviteUid) {
        super(source);
        this.memberList = memberList;
        this.roomGroup = roomGroup;
        this.inviteUid = inviteUid;
    }

}
