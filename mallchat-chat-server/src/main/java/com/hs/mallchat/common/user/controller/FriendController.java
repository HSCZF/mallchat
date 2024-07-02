package com.hs.mallchat.common.user.controller;

import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.request.PageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.domain.vo.response.PageBaseResp;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApplyReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendApproveReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendCheckReq;
import com.hs.mallchat.common.user.domain.vo.request.friend.FriendDeleteReq;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendApplyResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendCheckResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendResp;
import com.hs.mallchat.common.user.domain.vo.response.friend.FriendUnreadResp;
import com.hs.mallchat.common.user.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Author: CZF
 * @Create: 2024/6/24 - 9:43
 * Description: 好友相关接口
 */
@RestController
@RequestMapping("/capi/user/friend")
@Api(tags = "好友相关接口")
@Slf4j
public class FriendController {

    @Resource
    private FriendService friendService;

    @GetMapping("/check")
    @ApiOperation("批量判断是否是自己好友")
    public ApiResult<FriendCheckResp> check(@Valid FriendCheckReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.check(uid, request));
    }

    @GetMapping("/page")
    @ApiOperation("联系人列表")
    public ApiResult<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.friendList(uid, request));
    }

    @GetMapping("/apply")
    @ApiOperation("申请好友")
    public ApiResult<Void> apply(@Valid @RequestBody FriendApplyReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.apply(uid, request);
        return ApiResult.success();
    }

    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public ApiResult<PageBaseResp<FriendApplyResp>> page(@Valid PageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.pageApplyFriend(uid, request));
    }

    @GetMapping("/apply/unread")
    @ApiOperation("申请未读数")
    public ApiResult<FriendUnreadResp> unRead() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.unread(uid));
    }

    @GetMapping("/apply")
    @ApiOperation("审批同意")
    public ApiResult<Void> applyApprove(@Valid @RequestBody FriendApproveReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.applyApprove(uid, request);
        return ApiResult.success();
    }
    @DeleteMapping()
    @ApiOperation("删除好友")
    public ApiResult<Void> delete(@Valid @RequestBody FriendDeleteReq request) {
        Long uid = RequestHolder.get().getUid();
        friendService.deleteFriend(uid, request.getTargetUid());
        return ApiResult.success();
    }




}
