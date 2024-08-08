package com.hs.mallchat.common.user.service;

import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.IdRespVO;
import com.hs.mallchat.common.user.domain.entity.UserEmoji;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.mallchat.common.user.domain.vo.request.user.UserEmojiReq;
import com.hs.mallchat.common.user.domain.vo.response.user.UserEmojiResp;

import java.util.List;

/**
 * <p>
 * 用户表情包 服务类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-08-08
 */
public interface UserEmojiService{

    /**
     * 新增表情包
     * @param req 用户表情包
     * @param uid 用户ID
     * @return 表情包
     */
    ApiResult<IdRespVO> insert(UserEmojiReq req, Long uid);

    /**
     * 表情包列表
     * @param uid
     * @return 表情包列表
     */
    List<UserEmojiResp> list(Long uid);

    /**
     * 删除表情包
     * @param id
     * @param uid
     */
    void remove(long id, Long uid);
}
