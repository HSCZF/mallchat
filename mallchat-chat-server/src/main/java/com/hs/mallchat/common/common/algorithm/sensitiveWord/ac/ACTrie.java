package com.hs.mallchat.common.common.algorithm.sensitiveWord.ac;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * aho-corasick算法（又称AC自动机算法）
 * 非线程安全的AC字典树类
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 21:00
 */

@NotThreadSafe
public class ACTrie {
    // 根节点
    private ACTrieNode root;

    /**
     * 构造函数，初始化AC字典树
     *
     * @param words 用于构建AC字典树的单词列表
     */
    public ACTrie(List<String> words) {
        // 去除重复单词
        words = words.stream().distinct().collect(Collectors.toList());
        root = new ACTrieNode();
        // 遍历单词列表，添加到字典树中
        for (String word : words) {
            addWord(word);
        }
        // 初始化失败指针
        initFailover();
    }

    /**
     * 添加单词到AC字典树
     *
     * @param word 要添加的单词
     */
    public void addWord(String word) {
        ACTrieNode walkNode = root;
        char[] chars = word.toCharArray();
        for (int i = 0; i < word.length(); i++) {
            // 如果当前节点的子节点中不存在指定字符，则添加一个新的子节点。
            walkNode.addChildrenIfAbsent(chars[i]);
            walkNode = walkNode.childOf(chars[i]);
            walkNode.setDepth(i + 1);
        }
        walkNode.setLeaf(true);
    }

    /**
     * 初始化AC字典树的失败指针：failover
     * root
     * ├── a -> node1
     * │   ├── b -> node2
     * │   └── c -> node3
     * └── d -> node4
     * <p>
     * 初始化:
     * * * 将 node1 和 node4 添加到队列 queue 中。
     * 处理第一层:
     * * * 从队列中取出 node1，为其设置失败指针。
     * * * 将 node1 的子节点 node2 和 node3 添加到队列 queue 中。
     * * * 从队列中取出 node4，为其设置失败指针。
     * 处理第二层:
     * * * 从队列中取出 node2，为其设置失败指针。
     * * * 由于 node2 没有子节点，无需向队列添加新节点。
     * * * 从队列中取出 node3，为其设置失败指针。
     * * * 由于 node3 没有子节点，无需向队列添加新节点。
     * 通过这种方式，所有的节点都会被处理，并且失败指针会被正确设置
     */
    public void initFailover() {
        // 第一层的fail指针指向root
        Queue<ACTrieNode> queue = new LinkedList<>();
        Map<Character, ACTrieNode> children = root.getChildren();
        // root根节点下面的子节点，失败指针全部指向root
        for (ACTrieNode node : children.values()) {
            node.setFailover(root);
            queue.offer(node);
        }
        // 构建剩余层数节点的fail指针,利用层次遍历
        while (!queue.isEmpty()) {
            // 从队列中检索并移除队列头部的元素。
            ACTrieNode parentNode = queue.poll();
            for (Map.Entry<Character, ACTrieNode> entry : parentNode.getChildren().entrySet()) {
                ACTrieNode childNode = getAcTrieNode(entry, parentNode);
                queue.offer(childNode);
            }
        }
    }

    /**
     * 按照层次遍历的方法去处理字典树
     * @param entry
     * @param parentNode
     * @return
     */
    @NotNull
    private ACTrieNode getAcTrieNode(Map.Entry<Character, ACTrieNode> entry, ACTrieNode parentNode) {
        // 当前节点的值
        ACTrieNode childNode = entry.getValue();
        // 父节点的失败指针
        ACTrieNode parentNodeFailover = parentNode.getFailover();
        // 在树中找到以childNode为结尾的字符串的最长前缀匹配，failover指向了这个最长前缀匹配的父节点
        // 找父节点的失败指针，一直找不到则回退到root节点
        while (parentNodeFailover != null && (!parentNodeFailover.hasChild(entry.getKey()))) {
            parentNodeFailover = parentNodeFailover.getFailover();
        }
        // 回溯到了root节点
        if (parentNodeFailover == null) {
            childNode.setFailover(root);
        } else {
            // 更新当前节点的回退指针
            childNode.setFailover(parentNodeFailover.childOf(entry.getKey()));
        }
        return childNode;
    }

    /**
     * 在文本中查找所有匹配的单词
     *
     * @param text 待匹配的文本
     * @return 匹配结果列表，每个结果包含匹配单词的起始和结束位置
     */
    public List<MatchResult> matches(String text) {
        List<MatchResult> result = Lists.newArrayList();
        ACTrieNode walkNode = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (!walkNode.hasChild(c) && walkNode.getFailover() != null) {
                walkNode = walkNode.getFailover();
            }
            if (walkNode.hasChild(c)) {
                walkNode = walkNode.childOf(c);
                if (walkNode.isLeaf()) {
                    // 找到一个匹配单词，添加到结果列表中
                    result.add(new MatchResult(i - walkNode.getDepth() + 1, i + 1));
                    // 通过失败指针回退到根节点，继续匹配下一个单词
                    walkNode = walkNode.getFailover();
                }
            }
        }
        return result;
    }
}
