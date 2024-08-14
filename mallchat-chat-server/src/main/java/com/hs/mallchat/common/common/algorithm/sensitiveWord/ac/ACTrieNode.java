package com.hs.mallchat.common.common.algorithm.sensitiveWord.ac;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Description:
 * AC字典树节点类
 * AC字典树是一种用于快速检索字符串的树形数据结构，常用于文本检索、拼写检查等场景
 * 本类定义了AC字典树的节点结构，包括子节点、回退节点、深度和是否为叶子节点等信息
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 21:01
 */
@Getter
@Setter
public class ACTrieNode {

    // 子节点，使用Map存储，键为字符，值为指向的子节点
    private Map<Character, ACTrieNode> children = Maps.newHashMap();

    // 匹配过程中，如果模式串不匹配，模式串指针会回退到failover继续进行匹配
    private ACTrieNode failover = null;

    // 节点的深度，表示从根节点到当前节点的距离
    private int depth;

    // 标记当前节点是否为叶子节点，即是否是一个完整模式串的结尾
    private boolean isLeaf = false;

    /**
     * 如果当前节点的子节点中不存在指定字符，则添加一个新的子节点。
     * 这个方法利用了Map的computeIfAbsent方法，如果指定键（字符）不存在，则计算并返回新的值（节点）。
     *
     * @param c 待添加为子节点的字符
     */
    public void addChildrenIfAbsent(char c) {
        children.computeIfAbsent(c, (key) -> new ACTrieNode());
    }

    /**
     * 获取指定字符的子节点
     *
     * @param c 字符
     * @return 子节点，如果不存在则返回null
     */
    public ACTrieNode childOf(char c) {
        return children.get(c);
    }

    /**
     * 判断是否有指定字符的子节点
     *
     * @param c 字符
     * @return 如果存在该子节点返回true，否则返回false
     */
    public boolean hasChild(char c) {
        return children.containsKey(c);
    }

    /**
     * 节点的字符串表示形式
     * 主要用于调试和日志输出，方便查看节点的failover、depth和isLeaf状态
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "ACTrieNode{" +
                "failover=" + failover +
                ", depth=" + depth +
                ", isLeaf=" + isLeaf +
                '}';
    }
}
