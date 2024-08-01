package com.hs.mallchat.common.common.handler;

import lombok.extern.slf4j.Slf4j;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/31 - 10:56
 */
@Slf4j
public class GlobalUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final GlobalUncaughtExceptionHandler instance = new GlobalUncaughtExceptionHandler();

    private GlobalUncaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Exception in thread {} ", t.getName(), e);
    }

    public static GlobalUncaughtExceptionHandler getInstance() {
        return instance;
    }

}
