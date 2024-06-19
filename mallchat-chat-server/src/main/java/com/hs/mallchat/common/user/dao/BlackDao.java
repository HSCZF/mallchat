package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.user.domain.entity.Black;
import com.hs.mallchat.common.user.mapper.BlackMapper;
import com.hs.mallchat.common.user.service.IBlackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 黑名单 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-06-18
 */
@Service
public class BlackDao extends ServiceImpl<BlackMapper, Black> {

}
