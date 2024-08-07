package com.hs.mallchat.oss;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description: 各种对象存储可供选择
 *
 * @Author: CZF
 * @Create: 2024/8/7 - 16:14
 */
@Getter
@AllArgsConstructor
public enum OssType {
    /**
     * Minio 对象存储
     */
    MINIO("minio", 1),

    /**
     * 华为 OBS
     */
    OBS("obs", 2),

    /**
     * 腾讯 COS
     */
    COS("tencent", 3),

    /**
     * 阿里巴巴 SSO
     */
    ALIBABA("alibaba", 4),
    ;

    /**
     * 名称
     */
    final String name;
    /**
     * 类型
     */
    final int type;

}
