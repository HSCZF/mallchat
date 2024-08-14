package com.hs.mallchat.common.common.algorithm.sensitiveWord.acpro;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordFilter;
import io.micrometer.core.instrument.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Description:
 * 基于ACFilter的优化增强版本
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:44
 */
public class ACProFilter implements SensitiveWordFilter {

    private ACProTrie acProTrie;

    /**
     * 判断文本中是否存在敏感词
     *
     * @param text 文本
     * @return boolean
     */
    @Override
    public boolean hasSensitiveWord(String text) {
        if (StringUtils.isBlank(text)) return false;
        return !Objects.equals(filter(text), text);
    }

    /**
     * 过滤,敏感词替换
     *
     * @param text 文本
     * @return {@link String}
     */
    @Override
    public String filter(String text) {
        return acProTrie.match(text);
    }

    /**
     * 加载敏感词列表
     *
     * @param words 敏感词数组
     */
    @Override
    public void loadWord(List<String> words) {
        if (words == null) return;
        acProTrie = new ACProTrie();
        acProTrie.createACTrie(words);
    }
}
