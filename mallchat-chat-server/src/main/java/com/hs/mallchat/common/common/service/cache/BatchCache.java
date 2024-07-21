package com.hs.mallchat.common.common.service.cache;

import java.util.List;
import java.util.Map;

/**
 * Description: BatchCache接口
 *
 * @Author: CZF
 * @Create: 2024/7/18 - 15:10
 */
public interface BatchCache<IN, OUT> {
    /**
     * 获取单个
     */
    OUT get(IN req);

    /**
     * 获取批量
     */
    Map<IN, OUT> getBatch(List<IN> req);

    /**
     * 修改删除单个
     */
    void delete(IN req);

    /**
     * 修改删除多个
     */
    void deleteBatch(List<IN> req);

}
