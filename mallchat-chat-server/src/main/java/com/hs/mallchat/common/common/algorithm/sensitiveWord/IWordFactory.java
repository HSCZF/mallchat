package com.hs.mallchat.common.common.algorithm.sensitiveWord;

import java.util.List;

/**
 * Description:
 * 敏感词
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:38
 */
public interface IWordFactory {

    /**
     * 返回敏感词数据源
     *
     * @return 结果
     */
    List<String> getWordList();

}
