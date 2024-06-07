package com.hs.mallchat.common.user.controller;


import com.hs.mallchat.common.common.domain.dto.RequestInfo;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.interceptor.TokenInterceptor;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.vo.request.ModifyNameByReq;
import com.hs.mallchat.common.user.domain.vo.response.UserInfoResp;
import com.hs.mallchat.common.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-05-28
 */
@RestController
@RequestMapping("/capi/user")
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/userInfo")
    @ApiOperation("获取用户个人信息")
    public ApiResult<UserInfoResp> getUserInfo() {
        return ApiResult.success(userService.getUserInfo(RequestHolder.get().getUid()));
    }

    @PutMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult<UserInfoResp> modifyName(@Valid @RequestBody ModifyNameByReq req) {
        // 这里修改名字name长度超出了限制的6抛出了异常，但是没有捕获出来，需要配置一个全局的异常捕获
        userService.modifyName(RequestHolder.get().getUid(), req.getName());
        return ApiResult.success();
    }


}

