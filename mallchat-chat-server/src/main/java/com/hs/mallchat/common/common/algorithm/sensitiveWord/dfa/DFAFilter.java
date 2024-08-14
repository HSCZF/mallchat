package com.hs.mallchat.common.common.algorithm.sensitiveWord.dfa;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Description:
 * DFA:确定性有限状态自动机
 * 敏感词工具类，使用DFA（Deterministic Finite Automaton）算法进行敏感词过滤。
 * DFA算法是一种用于字符串匹配的算法，相较于传统的暴力匹配方法，DFA算法可以在不加载所有敏感词到内存的情况下，
 * 实现对敏感词的快速判断和替换。本类提供了加载敏感词、判断文本是否包含敏感词以及对敏感词进行替换的功能。
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:44
 */
public class DFAFilter implements SensitiveWordFilter {

    public DFAFilter() {
    }

    /**
     * 敏感词类，用于构建DFA中的状态节点。
     */
    private static class Word {
        // 当前字符
        private final char c;

        // 结束标志，表示以当前字符结尾的字符串是否是一个敏感词
        private boolean end;

        // 下一层级的敏感词字典，使用字符到节点的映射构建
        private Map<Character, Word> next;

        /**
         * 构造函数，初始化节点。
         *
         * @param c 当前节点代表的字符
         */
        public Word(char c) {
            this.c = c;
            this.next = new HashMap<>();
        }
    }

    // 敏感词字典的根节点
    private static Word root = new Word(' ');

    // 替代字符，用于替换敏感词
    private final static char replace = '*';

    // 遇到这些字符就会跳过，避免匹配
    private final static String skipChars = " !*-+_=,，.@;:；：。、？?（）()【】[]《》<>“”\"‘’";

    // 需要跳过的字符集合
    private final static Set<Character> skipSet = new HashSet<>();

    // 静态初始化块，初始化需要跳过的字符集合
    static {
        for (char c : skipChars.toCharArray()) {
            skipSet.add(c);
        }
    }

    /**
     * 获取DFAFilter实例。
     *
     * @return DFAFilter实例
     */
    public static DFAFilter getInstance() {
        return new DFAFilter();
    }

    /**
     * 判断文本是否包含敏感词。
     *
     * @param text 待检测的文本
     * @return true，如果文本包含敏感词；否则返回false
     */
    @Override
    public boolean hasSensitiveWord(String text) {
        if (StringUtils.isBlank(text)) return false;
        return !Objects.equals(filter(text), text);
    }

    /**
     * 对文本中的敏感词进行替换。
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    @Override
    public String filter(String text) {
        // 实现DFA算法进行敏感词的查找和替换
        StringBuilder result = new StringBuilder(text);
        int index = 0;
        while (index < result.length()) {
            char c = result.charAt(index);
            if (skip(c)) {
                index++;
                continue;
            }
            Word word = root;
            int start = index;
            boolean found = false;
            for (int i = index; i < result.length(); i++) {
                c = result.charAt(i);
                if (skip(c)) {
                    continue;
                }
                if (c >= 'A' && c <= 'Z') {
                    c += 32; // 将大写字母转换为小写
                }
                word = word.next.get(c);
                if (word == null) {
                    // 没有找到下个节点，跳出循环
                    break;
                }
                if (word.end) {
                    found = true;
                    for (int j = start; j <= i; j++) {
                        result.setCharAt(j, replace); // 替换敏感词
                    }
                    index = i;
                }
            }
            if (!found) {
                index++;
            }
        }
        return result.toString();
    }

    /**
     * 加载敏感词列表，构建DFA。
     *
     * @param words 敏感词列表
     */
    @Override
    public void loadWord(List<String> words) {
        if (!CollectionUtils.isEmpty(words)) {
            Word newRoot = new Word(' ');
            words.forEach(word -> loadWord(word, newRoot));
            root = newRoot;
        }
    }

    /**
     * 加载单个敏感词到DFA中。
     *
     * @param word 敏感词
     * @param root DFA的根节点
     */
    public void loadWord(String word, Word root) {
        if (StringUtils.isBlank(word)) {
            return;
        }
        Word current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 32; // 将大写字母转换为小写
            }
            if (skip(c)) {
                continue;
            }
            Word next = current.next.get(c);
            if (next == null) {
                next = new Word(c);
                // 将新的节点添加到当前节点的next映射中
                current.next.put(c, next);
            }
            current = next;
        }
        current.end = true; // 标记敏感词结束
    }

    /**
     * 判断是否需要跳过当前字符。
     *
     * @param c 待判断的字符
     * @return true，如果需要跳过；否则返回false
     */
    private boolean skip(char c) {
        return skipSet.contains(c);
    }

}
