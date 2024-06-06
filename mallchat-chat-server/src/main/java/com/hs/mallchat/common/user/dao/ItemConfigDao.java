package com.hs.mallchat.common.user.dao;

import com.hs.mallchat.common.user.domain.entity.ItemConfig;
import com.hs.mallchat.common.user.mapper.ItemConfigMapper;
import com.hs.mallchat.common.user.service.IItemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 功能物品配置表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-06-05
 */
@Service
public class ItemConfigDao extends ServiceImpl<ItemConfigMapper, ItemConfig> {

}
