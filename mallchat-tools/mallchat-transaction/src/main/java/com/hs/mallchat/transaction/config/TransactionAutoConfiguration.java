package com.hs.mallchat.transaction.config;

import com.hs.mallchat.transaction.annotation.SecureInvokeConfigurer;
import com.hs.mallchat.transaction.aspect.SecureInvokeAspect;
import com.hs.mallchat.transaction.dao.SecureInvokeRecordDao;
import com.hs.mallchat.transaction.mapper.SecureInvokeRecordMapper;
import com.hs.mallchat.transaction.service.MQProducer;
import com.hs.mallchat.transaction.service.SecureInvokeService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Description: 自动配置类
 * 用于整合事务管理组件，特别是与安全调用相关的配置。
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 15:51
 */

@Configuration
@EnableScheduling
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
@Import({SecureInvokeAspect.class, SecureInvokeRecordDao.class})
public class TransactionAutoConfiguration {

    /**
     * 可能为空的执行器引用，用于异步或安全调用任务。
     */
    @Nullable
    protected Executor executor;

    /**
     * 通过自动装配收集所有{@link SecureInvokeConfigurer}类型的bean。
     * 这个方法确保Spring容器中只有一个{@link SecureInvokeConfigurer}实例存在，
     * 如果找到多个，则抛出非法状态异常。
     *
     * @param configurers 提供器，用于获取所有{@link SecureInvokeConfigurer}类型的bean。
     */
    @Autowired
    void setConfigurers(ObjectProvider<SecureInvokeConfigurer> configurers) {
        Supplier<SecureInvokeConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokeConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one SecureInvokeConfigurer may exist");
            }
            return candidates.get(0);
        });
        // 获取配置器中的执行器，如果没有则使用公共ForkJoinPool。
        executor = Optional.ofNullable(configurer.get()).map(SecureInvokeConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }

    /**
     * 定义一个bean，用于创建安全调用服务实例。
     *
     * @param dao 数据访问对象，用于与数据库交互。
     * @return 返回一个配置好的安全调用服务实例。
     */
    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDao dao) {
        return new SecureInvokeService(dao, executor);
    }

    /**
     * 定义一个bean，用于创建消息队列生产者实例。
     *
     * @return 返回一个初始化的消息队列生产者实例。
     */
    @Bean
    public MQProducer getMQProducer() {
        return new MQProducer();
    }
}
