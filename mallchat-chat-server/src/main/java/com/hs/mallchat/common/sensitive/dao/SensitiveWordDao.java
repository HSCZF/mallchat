package com.hs.mallchat.common.sensitive.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.mallchat.common.sensitive.domain.SensitiveWord;
import com.hs.mallchat.common.sensitive.mapper.SensitiveWordMapper;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 敏感词DAO
 * @Author: CZF
 * @Create: 2024/8/9 - 15:34
 */
@Service
public class SensitiveWordDao extends ServiceImpl<SensitiveWordMapper, SensitiveWord> {
}
