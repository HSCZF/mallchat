package com.hs.mallchat.common.common.algorithm.sensitiveWord;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.ac.ACFilter;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.dfa.DFAFilter;

import java.util.List;

/**
 * Description:
 * 敏感词引导类
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:42
 */
public class SensitiveWordBs {

    /**
     * 私有化构造器
     */
    private SensitiveWordBs() {
    }

    /**
     * 配置了3个脱敏策略，
     * DFA算法：DFAFilter
     * AC自动机：ACFilter
     * AC自动机增强版：ACProFilter
     * private SensitiveWordFilter sensitiveWordFilter = DFAFilter.getInstance();
     * 将默认使用DFAFilter，需要替换的话，只需要将DFAFilter.getInstance()替换下面的即可
     * 1：new ACFilter();
     * 2：new ACProFilter();
     *
     * SensitiveWordConfig() 这里也要改，一样的替换
     */
    private SensitiveWordFilter sensitiveWordFilter = new ACFilter();


    /**
     * 敏感词列表
     */
    private IWordFactory wordDeny;

    public static SensitiveWordBs newInstance() {
        return new SensitiveWordBs();
    }

    /**
     * 初始化。根据配置，初始化对应的 map。比较消耗性能。
     *
     * @return
     */
    public SensitiveWordBs init() {
        List<String> words = wordDeny.getWordList();
        loadWord(words);
        return this;
    }

    /**
     * 过滤策略
     *
     * @param filter 过滤器
     * @return 结果
     */
    public SensitiveWordBs filterStrategy(SensitiveWordFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter can not be null");
        }
        this.sensitiveWordFilter = filter;
        return this;
    }

    public SensitiveWordBs sensitiveWord(IWordFactory wordFactory) {
        if (wordFactory == null) {
            throw new IllegalArgumentException("wordFactory can not be null");
        }
        this.wordDeny = wordFactory;
        return this;
    }

    /**
     * 有敏感词
     *
     * @param text
     * @return
     */
    public boolean hasSensitiveWord(String text) {
        return sensitiveWordFilter.hasSensitiveWord(text);
    }

    public String filter(String text) {
        return sensitiveWordFilter.filter(text);
    }


    /**
     * 加载敏感词列表
     *
     * @param words 敏感词数组
     */
    private void loadWord(List<String> words) {
        sensitiveWordFilter.loadWord(words);
    }


}
