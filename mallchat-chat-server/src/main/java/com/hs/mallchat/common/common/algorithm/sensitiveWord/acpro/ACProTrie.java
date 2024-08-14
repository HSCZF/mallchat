package com.hs.mallchat.common.common.algorithm.sensitiveWord.acpro;

import java.util.*;

/**
 * Description:
 * AC自动机 pro
 *
 * @Author: CZF
 * @Create: 2024/8/14 - 16:05
 */
public class ACProTrie {
    // 替代字符
    private final static char MASK = '*';

    // 根节点
    private Word root;

    // 定义AC自动机的节点类
    static class Word {
        // 判断是否是敏感词结尾
        boolean end = false;
        // 失败回调节点/状态
        Word failOver = null;
        // 记录字符偏移
        int depth = 0;
        // 下个自动机状态
        Map<Character, Word> next = new HashMap<>();

        // 判断当前节点是否有指定字符的子节点
        public boolean hasChild(char c) {
            return next.containsKey(c);
        }
    }

    /**
     * 创建自动机
     *
     * @param list 敏感词列表
     */
    public void createACTrie(List<String> list) {
        Word currentNode = new Word();
        root = currentNode;

        // 遍历敏感词列表，构建自动机
        for (String key : list) {
            currentNode = root;

            // 遍历单个敏感词的每一个字符
            for (int i = 0; i < key.length(); i++) {
                if (currentNode.next != null && currentNode.next.containsKey(key.charAt(i))) {
                    currentNode = currentNode.next.get(key.charAt(i));

                    // 如果是敏感词的最后一个字符，标记为结束
                    if (i == key.length() - 1) {
                        currentNode.end = true;
                    }
                } else {
                    Word map = new Word();

                    // 如果是敏感词的最后一个字符，标记为结束
                    if (i == key.length() - 1) {
                        map.end = true;
                    }

                    // 添加新节点到当前节点的子节点集合
                    currentNode.next.put(key.charAt(i), map);
                    currentNode = map;
                }

                // 设置当前节点的深度
                currentNode.depth = i + 1;
            }
        }

        // 初始化失败转移路径
        initFailOver();
    }

    /**
     * 初始化匹配失败回调节点/状态
     */
    private void initFailOver() {
        Queue<Word> queue = new LinkedList<>();

        // 将根节点的所有子节点加入队列，并设置它们的失败转移路径
        Map<Character, Word> children = root.next;
        for (Word node : children.values()) {
            node.failOver = root;
            queue.offer(node);
        }

        // 广度优先搜索，初始化所有节点的失败转移路径
        while (!queue.isEmpty()) {
            Word parentNode = queue.poll();

            // 遍历当前节点的所有子节点
            for (Map.Entry<Character, Word> entry : parentNode.next.entrySet()) {
                Word childNode = entry.getValue();
                Word failOver = parentNode.failOver;

                // 查找失败转移路径
                while (failOver != null && (!failOver.next.containsKey(entry.getKey()))) {
                    failOver = failOver.failOver;
                }

                // 设置失败转移路径
                if (failOver == null) {
                    childNode.failOver = root;
                } else {
                    childNode.failOver = failOver.next.get(entry.getKey());
                }

                // 将当前子节点加入队列
                queue.offer(childNode);
            }
        }
    }

    /**
     * 匹配敏感词并替换
     *
     * @param matchWord 待匹配的字符串
     * @return 替换敏感词后的字符串
     */
    public String match(String matchWord) {
        Word walkNode = root;
        char[] wordArray = matchWord.toCharArray();

        // 遍历待匹配字符串的每一个字符
        for (int i = 0; i < wordArray.length; i++) {
            // 当前路径不存在时，沿着失败转移路径回溯
            while (!walkNode.hasChild(wordArray[i]) && walkNode.failOver != null) {
                walkNode = walkNode.failOver;
            }

            // 如果当前路径存在
            if (walkNode.hasChild(wordArray[i])) {
                walkNode = walkNode.next.get(wordArray[i]);

                // 如果当前节点是敏感词的结尾
                if (walkNode.end) {
                    Word sentinelA = walkNode; // 记录当前节点
                    Word sentinelB = walkNode; // 记录end节点

                    // 探测是否还存在更长的敏感词
                    int k = i + 1;
                    boolean flag = false;
                    while (k < wordArray.length && sentinelA.hasChild(wordArray[k])) {
                        sentinelA = sentinelA.next.get(wordArray[k]);
                        k++;

                        // 如果找到更长的敏感词
                        if (sentinelA.end) {
                            sentinelB = sentinelA;
                            flag = true;
                        }
                    }

                    // 计算替换长度
                    int len = flag ? sentinelB.depth : walkNode.depth;

                    // 替换敏感词
                    while (len > 0) {
                        len--;
                        int index = flag ? i - walkNode.depth + 1 + len : i - len;
                        wordArray[index] = MASK;
                    }

                    // 更新索引位置
                    i += flag ? sentinelB.depth : 0;

                    // 更新当前节点
                    walkNode = flag ? sentinelB.failOver : walkNode.failOver;
                }
            }
        }

        // 返回替换后的字符串
        return new String(wordArray);
    }
}

