package com.hs.mallchat.common.common.service.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Iterables;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AbstractLocalCache是一个抽象类，用于实现基于Caffeine的本地缓存服务，
 * 特别是针对批量读取和写入操作。它使用泛型来处理不同类型的输入和输出数据。
 *
 * @param <IN> 输入类型
 * @param <OUT> 输出类型
 * @author czf
 * @Date: 2024-07-21
 */
public abstract class AbstractLocalCache<IN, OUT> implements BatchCache<IN, OUT> {

    // 存储输出类型的Class对象，用于泛型信息的反射
    private Class<OUT> outClass;
    // 存储输入类型的Class对象，用于泛型信息的反射
    private Class<IN> inClass;
    // Caffeine的LoadingCache实例，用于缓存数据
    private LoadingCache<IN, OUT> cache;

    // 默认构造函数，初始化缓存配置
    protected AbstractLocalCache() {
        init(60, 10 * 60, 1024);
    }

    // 构造函数，允许自定义缓存的刷新和过期时间以及最大容量
    protected AbstractLocalCache(long refreshSeconds, long expireSeconds, int maxSize) {
        init(refreshSeconds, expireSeconds, maxSize);
    }

    // 初始化方法，设置缓存配置并构建Caffeine的LoadingCache实例
    private void init(long refreshSeconds, long expireSeconds, int maxSize) {
        // 获取泛型超类的实际类型参数
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.outClass = (Class<OUT>) genericSuperclass.getActualTypeArguments()[1];
        this.inClass = (Class<IN>) genericSuperclass.getActualTypeArguments()[0];

        // 构建Caffeine缓存实例
        cache = Caffeine.newBuilder()
                .refreshAfterWrite(refreshSeconds, TimeUnit.SECONDS) // 设置缓存项刷新间隔
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS) // 设置缓存项过期时间
                .maximumSize(maxSize) // 设置缓存的最大容量
                .build(new CacheLoader<IN, OUT>() { // 构建缓存加载器
                    @Nullable
                    @Override
                    public OUT load(@NonNull IN key) throws Exception { // 单个key加载逻辑
                        return AbstractLocalCache.this.load(Collections.singletonList(key)).get(key);
                    }

                    @Override
                    public @NonNull Map<IN, OUT> loadAll(@NonNull Iterable<? extends IN> keys) throws Exception { // 批量加载逻辑
                        IN[] ins = Iterables.toArray(keys, inClass);
                        return AbstractLocalCache.this.load(Arrays.asList(ins));
                    }
                });
    }

    // 抽象方法，需要在子类中实现，用于加载或计算缓存数据
    protected abstract Map<IN, OUT> load(List<IN> req);

    // 实现BatchCache接口的get方法，从缓存中获取单个值
    @Override
    public OUT get(IN req) {
        return cache.get(req);
    }

    // 实现BatchCache接口的getBatch方法，从缓存中批量获取值
    @Override
    public Map<IN, OUT> getBatch(List<IN> req) {
        return cache.getAll(req);
    }

    // 实现BatchCache接口的delete方法，删除缓存中的单个键值对
    @Override
    public void delete(IN req) {
        cache.invalidate(req);
    }

    // 实现BatchCache接口的deleteBatch方法，批量删除缓存中的键值对
    @Override
    public void deleteBatch(List<IN> req) {
        cache.invalidateAll(req);
    }
}
