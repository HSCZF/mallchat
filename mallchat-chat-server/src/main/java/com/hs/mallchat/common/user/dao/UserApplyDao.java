package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.user.domain.entity.UserApply;
import com.hs.mallchat.common.user.mapper.UserApplyMapper;
import com.hs.mallchat.common.user.service.IUserApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户申请表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-06-24
 */
@Service
public class UserApplyDao extends ServiceImpl<UserApplyMapper, UserApply> implements IUserApplyService {

}
