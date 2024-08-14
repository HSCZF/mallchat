package com.hs.mallchat.common.sensitive.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description:
 * 敏感词
 * @Author: CZF
 * @Create: 2024/8/9 - 15:35
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sensitive_word")
public class SensitiveWord {
    private String word;
}
