package com.hs.mallchat.common.common.algorithm.sensitiveWord;

import java.util.List;

/**
 * Description:
 * 敏感词过滤
 * @Author: CZF
 * @Create: 2024/8/9 - 15:42
 */
public interface SensitiveWordFilter {

    /**
     * 判断文本中是否存在敏感词
     *
     * @param text 文本
     * @return boolean
     */
    boolean hasSensitiveWord(String text);

    /**
     * 过滤,敏感词替换
     *
     * @param text 文本
     * @return {@link String}
     */
    String filter(String text);

    /**
     * 加载敏感词列表
     *
     * @param words 敏感词数组
     */
    void loadWord(List<String> words);

}
