package com.hs.mallchat.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/7 - 16:14
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /**
     * 是否开启
     */
    Boolean enabled;

    /**
     * 存储对象服务器类型
     */
    OssType type;

    /**
     * OSS 访问端点，集群时需提供统一入口，API端口，不是控制台端口
     */
    String endpoint;

    /**
     * 用户名
     */
    String accessKey;

    /**
     * 密码
     */
    String secretKey;

    /**
     * 存储桶
     */
    String bucketName;
}
