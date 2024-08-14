package com.hs.mallchat.common.common.config;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.ac.ACFilter;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.dfa.DFAFilter;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordBs;
import com.hs.mallchat.common.sensitive.MyWordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * 敏感词配置类
 * 用于配置敏感词过滤的相关策略和工厂
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:46
 */

@Configuration
public class SensitiveWordConfig {

    /**
     * 注入自定义的敏感词工厂
     * 该工厂负责提供敏感词库的加载和管理
     */
    @Autowired
    private MyWordFactory myWordFactory;

    /**
     * 配置敏感词过滤器的实例
     * 该过滤器使用DFA算法进行敏感词过滤，并使用自定义的敏感词工厂提供敏感词数据
     * DFA算法：DFAFilter
     * AC自动机：ACFilter (我选择的是这个）
     * AC自动机增强版：ACProFilter
     * DFAFilter.getInstance()替换下面的即可：
     * 1：new ACFilter();
     * 2：new ACProFilter();
     * <p>
     * SensitiveWordBs里的sensitiveWordFilter也要改
     *
     * @return 配置好的敏感词过滤器实例
     */
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        // 项目启动时初始化敏感词过滤器。加载敏感词库，
        // 创建敏感词过滤器实例，并配置过滤策略和敏感词工厂
//        return SensitiveWordBs.newInstance()
//                .filterStrategy(DFAFilter.getInstance())
//                .sensitiveWord(myWordFactory)
//                .init();
        return SensitiveWordBs.newInstance()
                .filterStrategy(new ACFilter())
                .sensitiveWord(myWordFactory)
                .init();
    }

}
