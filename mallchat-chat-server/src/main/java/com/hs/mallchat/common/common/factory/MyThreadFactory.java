package com.hs.mallchat.common.common.factory;

import com.hs.mallchat.common.common.handler.GlobalUncaughtExceptionHandler;
import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

/**
 * @Author: CZF
 * @Create: 2024/6/4 - 9:21
 * Description: 装饰器模式
 */
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {

    /**
     * 线程工厂实例。
     * 该实例用于创建新的线程，线程的创建、命名、异常处理等可以通过线程工厂进行统一管理。
     */
    private ThreadFactory threadFactory;


    /**
     * 创建一个新线程并设置未捕获异常处理器。
     * <p>
     * 此方法重写了ThreadFactory接口的newThread方法，目的是在创建新线程时，为线程设置一个特定的未捕获异常处理器。
     * 这样做的目的是为了在线程中发生未捕获异常时，能够统一处理这些异常，增强程序的健壮性和可靠性。
     *
     * @param r 要在线程中执行的Runnable任务。
     * @return 返回一个配置了未捕获异常处理器的新线程。
     */
    @Override
    public Thread newThread(Runnable r) {
        // 使用threadFactory创建一个新的线程
        Thread thread = threadFactory.newThread(r);
        // 为新线程设置未捕获异常处理器
        thread.setUncaughtExceptionHandler(GlobalUncaughtExceptionHandler.getInstance());
        // 返回配置完毕的新线程
        return thread;
    }
}
