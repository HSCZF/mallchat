package com.hs.mallchat.common.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * @Author: CZF
 * @Create: 2024/6/9 - 15:41
 * Description:  配置类，用于启用和配置缓存。
 * 基于Caffeine构建缓存管理器，提供缓存的配置和管理。
 */

@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    /**
     * 定义一个名为"caffeineCacheManager"的Bean，用于管理缓存。
     * 该方法返回一个配置过的CaffeineCacheManager实例，它被标记为Primary，意味着在存在多个CacheManager时，它将作为默认选择。
     *
     * @return CaffeineCacheManager实例，配置了缓存的过期时间和最大容量。
     */
    @Bean("caffeineCacheManager")
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 配置缓存的过期时间、初始容量和最大容量
        // 方案一(常用)：定制化缓存Cache
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // 缓存项在写入5分钟后过期
                .initialCapacity(100) // 缓存的初始容量为100
                .maximumSize(200)); // 缓存的最大容量为200
        return cacheManager;
    }

}
