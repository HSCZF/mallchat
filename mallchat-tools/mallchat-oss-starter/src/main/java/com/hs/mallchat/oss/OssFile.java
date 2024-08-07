package com.hs.mallchat.oss;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/7 - 16:13
 */
@Data
@AllArgsConstructor
public class OssFile {
    /**
     * OSS 存储时文件路径
     */
    String ossFilePath;
    /**
     * 原始文件名
     */
    String originalFileName;
}