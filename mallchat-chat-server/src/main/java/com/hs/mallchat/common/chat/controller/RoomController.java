package com.hs.mallchat.common.chat.controller;


import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageMemberReq;
import com.hs.mallchat.common.chat.domain.vo.request.GroupAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.MemberDelReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberExitReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMemberListResp;
import com.hs.mallchat.common.chat.domain.vo.response.MemberResp;
import com.hs.mallchat.common.chat.service.IGroupMemberService;
import com.hs.mallchat.common.chat.service.RoomAppService;
import com.hs.mallchat.common.common.domain.vo.request.IdReqVO;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.domain.vo.response.IdRespVO;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.vo.response.ws.ChatMemberResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 房间表 前端控制器
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-22
 */
@RestController
@RequestMapping("/capi/room")
@Api(tags = "聊天室相关接口")
@Slf4j
public class RoomController {

    @Autowired
    private RoomAppService roomService;
    @Autowired
    private IGroupMemberService groupMemberService;

    @GetMapping("/public/group")
    @ApiOperation("群组详情")
    public ApiResult<MemberResp> groupDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getGroupDetail(uid, request.getId()));
    }

    @GetMapping("/group/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用")
    public ApiResult<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq request) {
        return ApiResult.success(roomService.getMemberList(request));
    }

    @DeleteMapping("/group/member")
    @ApiOperation("移除成员")
    public ApiResult<Void> delMember(@Valid @RequestBody MemberDelReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.delMember(uid, request);
        return ApiResult.success();
    }

    @PostMapping("/group")
    @ApiOperation("新增群组")
    public ApiResult<IdRespVO> addGroup(@Valid @RequestBody GroupAddReq request) {
        Long uid = RequestHolder.get().getUid();
        Long roomId = roomService.addGroup(uid, request);
        return ApiResult.success(IdRespVO.id(roomId));
    }

    @DeleteMapping("/group/member/exit")
    @ApiOperation("退出群聊")
    public ApiResult<Boolean> exitGroup(@Valid @RequestBody MemberExitReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.exitGroup(uid, request);
        return ApiResult.success();
    }

    @PostMapping("/group/member")
    @ApiOperation("邀请好友")
    public ApiResult<Void> addMember(@Valid @RequestBody MemberAddReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.addMember(uid, request);
        return ApiResult.success();
    }

    @GetMapping("/public/group/member/page")
    @ApiOperation("群成员列表")
    public ApiResult<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid MemberReq request) {
        // 有一个bug，空指针异常，GroupMember表现在还没有数据，查出来是null，第二次查询游标会变成 2_null
        // bug解决，ChatServiceImpl第348，349忘了赋值了
        //  timeCursor = cursorPage.getCursor();
        //  isLast = cursorPage.getIsLast();
        return ApiResult.success(roomService.getMemberPage(request));
    }

}

