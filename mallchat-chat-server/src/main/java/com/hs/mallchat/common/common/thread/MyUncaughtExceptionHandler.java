package com.hs.mallchat.common.common.thread;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: CZF
 * @Create: 2024/6/4 - 9:22
 */
@Slf4j
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {


    /**
     * 处理未捕获的异常。
     * 当线程中抛出的异常没有被处理时，此方法将被调用。目的是为了在日志中记录异常信息，以便于问题的追踪和调试。
     *
     * @param t 抛出异常的线程对象。
     * @param e 抛出的异常对象。
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 使用日志系统记录异常信息，以便于问题追踪和诊断。
        log.error("Exception in thread", e);
    }
}
