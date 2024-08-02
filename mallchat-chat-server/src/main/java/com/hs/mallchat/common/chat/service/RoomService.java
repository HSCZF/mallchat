package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.mallchat.common.chat.domain.entity.RoomFriend;
import com.hs.mallchat.common.chat.domain.entity.RoomGroup;

import java.util.List;

/**
 * <p>
 * 房间底层管理
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-22
 */
public interface RoomService{

    /**
     * 创建一个单聊房间
     */
    RoomFriend createFriendRoom(List<Long> list);

    RoomFriend getFriendRoom(Long uid1, Long uid2);

    /**
     * 禁用一个单聊房间
     */
    void disableFriendRoom(List<Long> uidList);


    /**
     * 创建一个群聊房间
     */
    RoomGroup createGroupRoom(Long uid);


}
