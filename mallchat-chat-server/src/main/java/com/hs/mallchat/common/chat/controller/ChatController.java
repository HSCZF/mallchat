package com.hs.mallchat.common.chat.controller;

import com.hs.mallchat.common.chat.domain.vo.request.ChatMessagePageReq;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.response.ChatMessageResp;
import com.hs.mallchat.common.chat.service.ChatService;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.hs.mallchat.common.user.service.cache.UserCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

/**
 * Description: 群聊相关接口
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 17:13
 */
@RestController
@RequestMapping("/capi/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserCache userCache;

    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getMsgPage(@Valid ChatMessagePageReq request) {
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(request, RequestHolder.get().getUid());
        filterBlackMsg(msgPage);
        return ApiResult.success(msgPage);
    }

    private void filterBlackMsg(CursorPageBaseResp<ChatMessageResp> memberPage) {
        Set<String> blackMembers = getBlackUidSet();
        memberPage.getList().removeIf(a -> blackMembers.contains(a.getFromUser().getUid().toString()));
    }

    private Set<String> getBlackUidSet() {
        return userCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());
    }

    /**
     * 发消息
     */
    @PostMapping("/msg")
    @ApiOperation("发消息")
    public ApiResult<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq request) {
        // todo 先不加自定义注解FrequencyControl，后面再加
        Long msgId = chatService.sendMsg(request, RequestHolder.get().getUid());
        return ApiResult.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }

}
