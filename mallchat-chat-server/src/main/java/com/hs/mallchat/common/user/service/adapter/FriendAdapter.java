package com.hs.mallchat.common.user.service.adapter;

import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.entity.UserApply;
import com.hs.mallchat.common.user.domain.entity.UserFriend;
import com.hs.mallchat.common.user.domain.enums.ApplyReadStatusEnum;
import com.hs.mallchat.common.user.domain.enums.ApplyStatusEnum;
import com.hs.mallchat.common.user.domain.enums.ApplyTypeEnum;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApplyReq;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendApplyResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendResp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: CZF
 * @Create: 2024/6/24 - 14:57
 * Description: 好友适配器
 */
public class FriendAdapter {

    public static UserApply buildFriendApply(Long uid, FriendApplyReq request) {
        UserApply userApplyNew = new UserApply();
        userApplyNew.setUid(uid);
        userApplyNew.setMsg(request.getMsg());
        userApplyNew.setType(ApplyTypeEnum.ADD_FRIEND.getCode());
        userApplyNew.setTargetId(request.getTargetUid());
        userApplyNew.setStatus(ApplyStatusEnum.WAIT_APPROVAL.getCode());
        userApplyNew.setReadStatus(ApplyReadStatusEnum.UNREAD.getCode());
        return userApplyNew;
    }


    public static List<FriendApplyResp> buildFriendApplyList(List<UserApply> records) {
        return records.stream().map(userApply -> {
            FriendApplyResp friendApplyResp = new FriendApplyResp();
            friendApplyResp.setUid(userApply.getUid());
            friendApplyResp.setType(userApply.getType());
            friendApplyResp.setApplyId(userApply.getId());
            friendApplyResp.setMsg(userApply.getMsg());
            friendApplyResp.setStatus(userApply.getStatus());
            return friendApplyResp;
        }).collect(Collectors.toList());
    }

    public static List<FriendResp> buildFriend(List<UserFriend> list, List<User> userList) {

        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        return list.stream().map(userFriend -> {
            FriendResp resp = new FriendResp();
            resp.setUid(userFriend.getFriendUid());
            User user = userMap.get(userFriend.getFriendUid());
            if (Objects.nonNull(user)) {
                resp.setActiveStatus(user.getActiveStatus());
            }
            return resp;
        }).collect(Collectors.toList());
    }
}
