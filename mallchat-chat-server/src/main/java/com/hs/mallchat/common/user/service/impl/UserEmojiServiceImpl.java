package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.annotation.RedissonLock;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.IdRespVO;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.dao.UserEmojiDao;
import com.hs.mallchat.common.user.domain.entity.UserEmoji;
import com.hs.mallchat.common.user.domain.vo.request.user.UserEmojiReq;
import com.hs.mallchat.common.user.domain.vo.response.user.UserEmojiResp;
import com.hs.mallchat.common.user.service.UserEmojiService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * 用户表情包
 *
 * @Author: CZF
 * @Create: 2024/8/8 - 9:53
 */
@Service
@Slf4j
public class UserEmojiServiceImpl implements UserEmojiService {

    @Autowired
    private UserEmojiDao userEmojiDao;

    /**
     * 新增表情包
     *
     * @param req 用户表情包
     * @param uid 用户ID
     * @return 表情包
     */
    @Override
    @RedissonLock(key = "#uid")
    public ApiResult<IdRespVO> insert(UserEmojiReq req, Long uid) {
        int count = userEmojiDao.countByUid(uid);
        AssertUtil.isFalse(count > 30, "最多只能添加30个表情哦~~");
        // 校验表情是否存在
        Integer existsCount = userEmojiDao.lambdaQuery()
                .eq(UserEmoji::getExpressionUrl, req.getExpressionUrl())
                .eq(UserEmoji::getUid, uid)
                .count();
        AssertUtil.isFalse(existsCount > 0, "当前表情已存在哦~~");
        UserEmoji insert = UserEmoji.builder()
                .uid(uid)
                .expressionUrl(req.getExpressionUrl())
                .build();
        userEmojiDao.save(insert);
        return ApiResult.success(IdRespVO.id(insert.getId()));
    }

    /**
     * 表情包列表
     *
     * @param uid
     * @return 表情包列表
     */
    @Override
    public List<UserEmojiResp> list(Long uid) {
        return userEmojiDao.listByUid(uid).stream()
                .map(a -> UserEmojiResp.builder()
                        .id(a.getId())
                        .expressionUrl(a.getExpressionUrl())
                        .build()
                ).collect(Collectors.toList());
    }

    /**
     * 删除表情包
     *
     * @param id
     * @param uid
     */
    @Override
    public void remove(long id, Long uid) {
        UserEmoji userEmoji = userEmojiDao.getById(id);
        AssertUtil.isNotEmpty(userEmoji, "表情不能为空");
        AssertUtil.equal(userEmoji.getUid(), uid, "小黑子，别人表情不是你能删的");
        userEmojiDao.removeById(id);
    }
}
