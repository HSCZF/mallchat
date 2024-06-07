package com.hs.mallchat.common.common.utils;

import com.hs.mallchat.common.common.domain.dto.RequestInfo;

/**
 * @Author: CZF
 * @Create: 2024/6/7 - 10:10
 * Description: 请求上下文
 */
public class RequestHolder {

    private static final ThreadLocal<RequestInfo> threadLocal = new ThreadLocal<RequestInfo>();

    public static void set(RequestInfo requestInfo) {
        threadLocal.set(requestInfo);
    }

    public static RequestInfo get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

}
