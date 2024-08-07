package com.hs.mallchat.oss;

import io.minio.MinioClient;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description: 配置MinIO客户端和模板的类
 * 本配置类基于Spring Boot的配置属性，仅在配置文件中指定的条件满足时启用（oss.enabled）
 *  # OSS配置
 * oss.enabled=true，oss.type= minio，符合这个@ConditionalOnProperty注解配置类才会被激活
 *
 * @Author: CZF
 * @Create: 2024/8/7 - 16:14
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnExpression("${oss.enabled}")
@ConditionalOnProperty(value = "oss.type", havingValue = "minio")
public class MinIOConfiguration {

    /**
     * 创建MinIO客户端实例
     * <p>
     * 本方法仅在没有MinioClient实例存在时创建一个新的实例，避免重复创建
     * 使用SneakyThrows跳过异常处理，简化代码
     *
     * @param ossProperties 包含OSS相关配置的属性对象
     * @return 新创建的MinioClient实例
     */
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean(MinioClient.class)
    public MinioClient minioClient(OssProperties ossProperties) {
        return MinioClient.builder()
                .endpoint(ossProperties.getEndpoint())
                .credentials(ossProperties.getAccessKey(), ossProperties.getSecretKey())
                .build();
    }

    /**
     * 创建MinIOTemplate实例
     * <p>
     * 本方法仅在存在MinioClient实例且没有MinIOTemplate实例时创建一个新的实例
     * 提供一个操作MinIO的模板，方便进行批量操作
     *
     * @param minioClient   用于操作MinIO的客户端
     * @param ossProperties 包含OSS相关配置的属性对象
     * @return 新创建的MinIOTemplate实例
     */
    @Bean
    @ConditionalOnBean({MinioClient.class})
    @ConditionalOnMissingBean(MinIOTemplate.class)
    public MinIOTemplate minioTemplate(MinioClient minioClient, OssProperties ossProperties) {
        return new MinIOTemplate(minioClient, ossProperties);
    }

}
