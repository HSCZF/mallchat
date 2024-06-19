package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.user.domain.entity.Role;
import com.hs.mallchat.common.user.mapper.RoleMapper;
import com.hs.mallchat.common.user.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-06-18
 */
@Service
public class RoleDao extends ServiceImpl<RoleMapper, Role> {

}
