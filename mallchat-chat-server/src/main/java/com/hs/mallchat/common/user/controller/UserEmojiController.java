package com.hs.mallchat.common.user.controller;


import com.hs.mallchat.common.common.domain.vo.request.IdReqVO;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.IdRespVO;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.entity.UserEmoji;
import com.hs.mallchat.common.user.domain.vo.request.user.UserEmojiReq;
import com.hs.mallchat.common.user.domain.vo.response.user.UserEmojiResp;
import com.hs.mallchat.common.user.service.UserEmojiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 用户表情包 前端控制器
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-08-08
 */
@RestController
@RequestMapping("/capi/user/emoji")
@Api(tags = "用户表情包管理相关接口")
public class UserEmojiController {

    /**
     * 用户表情包 Service
     */
    @Resource
    private UserEmojiService emojiService;

    /**
     * 新增表情包
     *
     * @param req 用户表情包
     * @return 表情包
     **/
    @PostMapping()
    @ApiOperation("新增表情包")
    public ApiResult<IdRespVO> insertEmojis(@Valid @RequestBody UserEmojiReq req) {
        return emojiService.insert(req, RequestHolder.get().getUid());
    }

    /**
     * 表情包列表
     */
    @GetMapping("/list")
    @ApiOperation("表情包列表")
    public ApiResult<List<UserEmojiResp>> getEmojisPage() {
        return ApiResult.success(emojiService.list(RequestHolder.get().getUid()));
    }

    /**
     * 删除表情包
     *
     * @param reqVO
     * @return
     */
    @DeleteMapping()
    @ApiOperation("删除表情包")
    public ApiResult<Void> deleteEmojis(@Valid @RequestBody IdReqVO reqVO) {
        emojiService.remove(reqVO.getId(), RequestHolder.get().getUid());
        return ApiResult.success();
    }


}

