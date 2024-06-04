package com.hs.mallchat.common.common.config;

import com.hs.mallchat.common.common.thread.MyThreadFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: CZF
 * @Create: 2024/6/4 - 9:12
 * Description: 统一管理配置线程池
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer {

    /**
     * 项目共用线程池
     */
    public static final String MALLCHAT_EXECUTOR = "mallchatExecutor";
    /**
     * websocket通信线程池
     */
    public static final String WS_EXECUTOR = "websocketExecutor";

    @Override
    public Executor getAsyncExecutor() {
        return AsyncConfigurer.super.getAsyncExecutor();
    }

    /**
     * 配置一个名为MALLCHAT_EXECUTOR的线程池 bean。
     * 这个线程池主要用于处理mallchat相关的异步任务。
     *
     * @return ThreadPoolTaskExecutor 实例，配置了特定的线程池参数。
     */
    @Bean(MALLCHAT_EXECUTOR)
    @Primary
    public ThreadPoolTaskExecutor mallchatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 线程池在关闭时等待所有任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 设置核心线程数为10
        executor.setCorePoolSize(10);

        // 设置最大线程数为10
        executor.setMaxPoolSize(10);

        // 设置队列容量为200
        executor.setQueueCapacity(200);

        // 设置线程名前缀
        executor.setThreadNamePrefix("mallchat-executor-");

        // 设置拒绝策略为CallerRunsPolicy，当队列满时，由调用者线程处理任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 设置自定义的线程工厂
        executor.setThreadFactory(new MyThreadFactory(executor));

        // 初始化线程池
        executor.initialize();

        return executor;
    }


    /**
     * 配置WebSocket任务执行器。
     * 此执行器用于处理WebSocket相关的后台任务，确保WebSocket连接的正常运行和维护。
     *
     * @return ThreadPoolTaskExecutor 实例，配置了用于WebSocket任务的线程池参数。
     */
    @Bean(WS_EXECUTOR)
    public ThreadPoolTaskExecutor websocketExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置在关闭时等待所有任务完成，确保WebSocket任务被优雅地处理
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 配置核心线程池大小为16，保证最小的线程数量
        executor.setCorePoolSize(16);

        // 配置最大线程池大小为16，限制线程池的扩展规模
        executor.setMaxPoolSize(16);

        // 配置队列容量为1000，用于存储待处理的任务
        executor.setQueueCapacity(1000);

        // 设置线程名前缀，便于识别WebSocket相关的线程
        executor.setThreadNamePrefix("websocket-executor-");

        // 设置拒绝策略为丢弃策略，当线程池和队列满时，新任务将被直接丢弃
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());

        // 设置自定义线程工厂，用于创建WebSocket任务执行器的线程
        executor.setThreadFactory(new MyThreadFactory(executor));

        // 初始化线程池，使其配置生效
        executor.initialize();

        return executor;
    }


}
