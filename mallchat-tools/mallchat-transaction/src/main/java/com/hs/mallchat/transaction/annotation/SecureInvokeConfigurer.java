package com.hs.mallchat.transaction.annotation;


import javax.annotation.Nullable;
import java.util.concurrent.Executor;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 16:00
 */
public interface SecureInvokeConfigurer {

    /**
     * 获取异步执行器
     * @return 返回一个线程池
     * com.hs.mallchat.common.common.config包下的ThreadPoolConfig#getSecureInvokeExecutor()
     */
    @Nullable
    default Executor getSecureInvokeExecutor() {
        return null;
    }

}
