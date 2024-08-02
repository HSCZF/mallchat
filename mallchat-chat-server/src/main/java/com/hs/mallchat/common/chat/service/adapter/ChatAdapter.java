package com.hs.mallchat.common.chat.service.adapter;

import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.chat.domain.entity.RoomFriend;
import com.hs.mallchat.common.chat.domain.enums.HotFlagEnum;
import com.hs.mallchat.common.chat.domain.enums.RoomTypeEnum;
import com.hs.mallchat.common.common.domain.enums.NormalOrNoEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/2 - 15:49
 */
public class ChatAdapter {

    public static final String SEPARATOR = ",";


    public static String generateRoomKey(List<Long> uidList) {
        return uidList.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARATOR));
    }

    public static Room buildRoom(RoomTypeEnum typeEnum) {
        Room room = new Room();
        room.setType(typeEnum.getType());
        room.setHotFlag(HotFlagEnum.NOT.getType());
        return room;
    }

    public static RoomFriend buildFriendRoom(Long roomId, List<Long> uidList) {
        List<Long> collect = uidList.stream()
                .sorted()
                .collect(Collectors.toList());
        RoomFriend roomFriend = new RoomFriend();
        roomFriend.setRoomId(roomId);
        roomFriend.setUid1(collect.get(0));
        roomFriend.setUid2(collect.get(1));
        roomFriend.setRoomKey(generateRoomKey(uidList));
        roomFriend.setStatus(NormalOrNoEnum.NORMAL.getStatus());
        return roomFriend;
    }
}
