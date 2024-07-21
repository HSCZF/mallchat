package com.hs.mallchat.common.user.service.cache;

import com.hs.mallchat.common.user.dao.ItemConfigDao;
import com.hs.mallchat.common.user.domain.entity.ItemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: CZF
 * @Create: 2024/6/9 - 15:50
 * Description: 缓存类，用于缓存和管理物品配置信息。
 * 使用缓存来提高对物品配置信息的访问效率。
 */

@Component
public class ItemCache {

    /**
     * 自动注入物品配置数据访问对象，用于从数据库中获取物品配置信息。
     */
    @Autowired
    private ItemConfigDao itemConfigDao;

    /**
     * 通过类型获取物品配置信息列表，并将结果缓存。
     * 
     * @param itemType 物品类型ID，用于查询特定类型的物品配置信息。
     * @return 物品配置信息列表，包含指定类型的物品配置。
     * @Cacheable 注解用于指示该方法的结果应被缓存。
     *             cacheNames 指定了缓存的名称为 "item"。
     *             key 使用 SpEL 表达式生成缓存键，格式为 "itemsByType:" 加上物品类型。
     */
    @Cacheable(cacheNames = "item", key = "'itemsByType:'+#itemType")
    public List<ItemConfig> getByType(Integer itemType) {
        return itemConfigDao.getByType(itemType);
    }

    @Cacheable(cacheNames = "item", key = "'item:'+#itemId")
    public ItemConfig getById(Long itemId) {
        return itemConfigDao.getById(itemId);
    }

    /**
     * 清除指定类型物品的配置缓存。
     *
     * 当需要更新特定类型物品的配置或者该类型物品的信息发生变化时，调用此方法来清除对应的缓存数据。
     * 这样做是为了确保后续访问该类型物品的信息时，能够获取到最新的数据，避免使用过时的缓存数据。
     *
     * @param itemType 物品类型的ID，用于指定需要清除缓存的物品类型。
     *                 此参数作为缓存键的一部分，用于精确定位到需要清除的缓存项。
     * @CacheEvict 注解用于指示该方法执行后应立即将指定缓存项从缓存中移除。
     *              这里使用了SpEL表达式"'itemsByType:'+#itemType"来构造缓存键，确保能够根据物品类型动态清除缓存。
     */
    @CacheEvict(cacheNames = "item", key = "'itemsByType:'+#itemType")
    public void evictByType(Integer itemType) {
        // 不需要编写代码
    }


}

