package com.hs.mallchat.common.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: CZF
 * @Create: 2024/6/3 - 11:00
 */
@Configuration
public class RedissonConfig {

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 创建Redisson客户端实例。
     * Redisson是一个基于Redis客户端的Java框架，提供了一套丰富的Redis客户端API。
     * 该方法通过配置Redisson客户端的连接信息，如服务器地址、密码和数据库索引，来初始化并返回一个Redisson客户端实例。
     * 使用Redisson客户端可以方便地进行Redis的操作。
     *
     * @return RedissonClient实例，用于与Redis服务器进行通信。
     */
    @Bean
    public RedissonClient redissonClient() {
        // 创建Redisson配置对象
        Config config = new Config();
        // 配置单服务器模式，设置Redis服务器的地址、密码和数据库索引
        config.useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase());
        // 根据配置创建并返回Redisson客户端实例
        return Redisson.create(config);
    }


}
