package com.hs.mallchat.common.sensitive;

import com.hs.mallchat.common.common.algorithm.sensitiveWord.IWordFactory;
import com.hs.mallchat.common.sensitive.dao.SensitiveWordDao;
import com.hs.mallchat.common.sensitive.domain.SensitiveWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/9 - 15:35
 */
@Component
public class MyWordFactory implements IWordFactory {

    @Autowired
    private SensitiveWordDao sensitiveWordDao;

    /**
     * 返回敏感词数据源
     *
     * @return 结果
     */
    @Override
    public List<String> getWordList() {
        return sensitiveWordDao.list()
                .stream()
                .map(SensitiveWord::getWord)
                .collect(Collectors.toList());
    }
}
