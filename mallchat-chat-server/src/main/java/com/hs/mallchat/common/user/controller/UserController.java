package com.hs.mallchat.common.user.controller;


import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.common.utils.RequestHolder;
import com.hs.mallchat.common.user.domain.dto.ItemInfoDTO;
import com.hs.mallchat.common.user.domain.dto.SummeryInfoDTO;
import com.hs.mallchat.common.user.domain.enums.RoleEnum;
import com.hs.mallchat.common.user.domain.vo.request.user.*;
import com.hs.mallchat.common.user.domain.vo.response.user.BadgeResp;
import com.hs.mallchat.common.user.domain.vo.response.user.UserInfoResp;
import com.hs.mallchat.common.user.service.IRoleService;
import com.hs.mallchat.common.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

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
    @Autowired
    private IRoleService iRoleService;

    @GetMapping("/userInfo")
    @ApiOperation("用户详情")
    public ApiResult<UserInfoResp> getUserInfo() {
        return ApiResult.success(userService.getUserInfo(RequestHolder.get().getUid()));
    }

    @PostMapping("/public/summary/userInfo/batch")
    @ApiOperation("用户聚合信息-返回的代表需要刷新的")
    public ApiResult<List<SummeryInfoDTO>> getSummeryUserInfo(@Valid @RequestBody SummeryInfoReq req) {
        return ApiResult.success(userService.getSummeryUserInfo(req));
    }

    @PostMapping("/public/badges/batch")
    @ApiOperation("徽章聚合信息-返回的代表需要刷新的")
    public ApiResult<List<ItemInfoDTO>> getItemInfo(@Valid @RequestBody ItemInfoReq req) {
        return ApiResult.success(userService.getItemInfo(req));
    }


    @PutMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult<UserInfoResp> modifyName(@Valid @RequestBody ModifyNameByReq req) {
        // 这里修改名字name长度超出了限制的6抛出了异常，但是没有捕获出来，需要配置一个全局的异常捕获
        userService.modifyName(RequestHolder.get().getUid(), req.getName());
        return ApiResult.success();
    }

    /**
     * 这里item_config表的describe是一个关键字，需要在实体类修改，加个反引号``
     * SELECT  id ,type,img,describe,create_time,update_time  FROM item_config WHERE type = 2
     * 出现bug：[Err] 1064 - You have an error in your SQL syntax;
     * check the manual that corresponds to your MySQL server version for the right syntax to use near 'describe,create_time,update_time  FROM item_config WHERE type = 2' at line 1
     * * 1. 错误原因：describe是关键字，不能作为字段名
     * * 2. 解决方案：使用别名，加个反引号``, @TableField("`describe`")
     * * SELECT  id,type,img,`describe`,create_time,update_time  FROM item_config WHERE type = 2
     */
    @GetMapping("/badges")
    @ApiOperation("可选徽章预览")
    public ApiResult<List<BadgeResp>> badges() {
        return ApiResult.success(userService.badges(RequestHolder.get().getUid()));
    }

    @PutMapping("/badge")
    @ApiOperation("佩戴徽章")
    public ApiResult<Map<String, Object>> wearingBadge(@Valid @RequestBody WearingBadgeReq req) {
        userService.wearingBadge(RequestHolder.get().getUid(), req.getItemId());
        Map<String, Object> exchange = new HashMap<>();
        exchange.put("exchange", "更换成功");
        return ApiResult.success(exchange);
    }

    @PutMapping("/black")
    @ApiOperation("拉黑用户")
    public ApiResult<Map<String, Object>> black(@Valid @RequestBody BlackReq req) {
        Long uid = RequestHolder.get().getUid();
        boolean hasPower = iRoleService.hasPower(uid, RoleEnum.ADMIN);
        AssertUtil.isTrue(hasPower, "抹茶管理员没有权限");
        userService.black(req);
        Map<String, Object> exchange = new HashMap<>();
        exchange.put("exchangeBlack", "拉黑成功");
        return ApiResult.success(exchange);
    }

}

