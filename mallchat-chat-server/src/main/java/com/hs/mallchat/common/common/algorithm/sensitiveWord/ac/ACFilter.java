package com.hs.mallchat.common.common.algorithm.sensitiveWord.ac;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordFilter;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.ac.ACTrie;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.ac.MatchResult;
import org.HdrHistogram.ConcurrentHistogram;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Description:
 * 基于ac自动机实现的敏感词过滤工具类
 * 可以用来替代{@link ConcurrentHistogram}
 * 为了兼容提供了相同的api接口 {@code hasSensitiveWord}
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:43
 */
public class ACFilter implements SensitiveWordFilter {

    // 替代字符
    private final static char mask_char = '*';

    // 全局AC字典树实例，用于敏感词检测和替换
    private static ACTrie ac_trie = null;


    /**
     * 判断文本中是否存在敏感词
     *
     * @param text 文本
     * @return boolean 如果存在敏感词返回true，否则返回false
     */
    @Override
    public boolean hasSensitiveWord(String text) {
        if (StringUtils.isBlank(text)) return false;
        return !Objects.equals(filter(text), text);
    }

    /**
     * 过滤文本中的敏感词并替换
     *
     * @param text 待过滤的文本
     * @return 过滤后的{@link String}
     */
    @Override
    public String filter(String text) {
        if (StringUtils.isBlank(text)) return text;
        // 获取文本中所有匹配的敏感词
        List<MatchResult> matchResults = ac_trie.matches(text);
        // 创建一个可变的字符串对象，用于存储过滤后的文本
        StringBuffer result = new StringBuffer(text);
        // matchResults是按照startIndex排序的，因此可以通过不断更新endIndex最大值的方式算出尚未被替代部分
        int endIndex = 0;
        for (MatchResult matchResult : matchResults) {
            endIndex = Math.max(endIndex, matchResult.getEndIndex());
            // 替换敏感词
            replaceBetween(result, matchResult.getStartIndex(), endIndex);
        }
        return result.toString();
    }

    /**
     * 在指定的字符串缓冲区中，替换两个索引之间的字符
     *
     * @param buffer 字符串缓冲区
     * @param startIndex 替换的起始索引
     * @param endIndex 替换的结束索引
     */
    private static void replaceBetween(StringBuffer buffer, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; i++) {
            buffer.setCharAt(i, mask_char);
        }
    }

    /**
     * 加载敏感词列表到AC字典树中
     *
     * @param words 敏感词数组
     */
    @Override
    public void loadWord(List<String> words) {
        if (words == null) return;
        // 创建并初始化AC字典树
        ac_trie = new ACTrie(words);
    }
}
