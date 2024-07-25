package com.hs.mallchat.transaction.service;

import java.util.Objects;

/**
 * Description:
 * 安全调用上下文持有者，用于在多线程环境中标识当前线程是否处于安全调用过程中。
 * 通过ThreadLocal存储线程私有的调用状态，避免线程间的状态污染。
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 11:03
 */
public class SecureInvokeHolder {

    /**
     * ThreadLocal变量，用于保存每个线程的安全调用状态。
     * 存储Boolean类型，以便明确表示线程是否正在执行安全调用。
     */
    private static final ThreadLocal<Boolean> INVOKE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 检查当前线程是否正在进行安全调用。
     *
     * @return 如果线程正在进行安全调用返回true，否则返回false。
     */
    public static Boolean isInvoking() {
        return Objects.nonNull(INVOKE_THREAD_LOCAL.get());
    }

    /**
     * 标记当前线程为正在执行安全调用。
     * 调用此方法后，isInvoking()将返回true。
     */
    public static void setInvoking() {
        INVOKE_THREAD_LOCAL.set(Boolean.TRUE);
    }

    /**
     * 清除当前线程的安全调用状态。
     * 调用此方法后，isInvoking()将返回false。
     */
    public static void invoked() {
        INVOKE_THREAD_LOCAL.remove();
    }

}

