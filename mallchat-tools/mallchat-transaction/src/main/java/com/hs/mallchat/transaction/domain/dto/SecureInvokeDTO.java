package com.hs.mallchat.transaction.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 安全调用数据传输对象，用于封装远程或安全环境下方法调用的必要信息。
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 11:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecureInvokeDTO {

    /**
     * 目标类的全限定名，即包含包名的类名。
     */
    private String className;

    /**
     * 目标方法的名称。
     */
    private String methodName;

    /**
     * 方法参数类型的字符串表示，通常用于动态解析方法签名。
     */
    private String parameterTypes;

    /**
     * 方法调用的参数值，可能需要序列化以适应网络传输或反序列化以恢复原始参数。
     */
    private String args;
}
