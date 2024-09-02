package com.hs.mallchat.common.common.service.frequencycontrol;

import com.hs.mallchat.common.common.domain.dto.FrequencyControlDTO;
import com.hs.mallchat.common.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.hs.mallchat.common.common.service.frequencycontrol.FrequencyControlStrategyFactory.TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;

/**
 * Description:
 * 抽象类频控服务 -使用redis实现 固定时间内不超过固定次数的限流类
 * 子类，继承抽象频率控制服务类，用于具体实现频率控制策略
 *
 * @Author: CZF
 * @Create: 2024/8/30 - 15:37
 */

@Slf4j
@Service
public class TotalCountWithInFixTimeFrequencyController extends AbstractFrequencyControlService<FrequencyControlDTO> {
    // 获取策略名称，这里应返回具体策略的标识符
    @Override
    protected String getStrategyName() {
        return TOTAL_COUNT_WITH_IN_FIX_TIME_FREQUENCY_CONTROLLER;
    }

    // 向频率控制统计计数器中添加计数
    // 解释：此方法用于在频率控制映射中为每个键值对增加计数
    @Override
    protected void addFrequencyControlStatisticsCount(Map<String, FrequencyControlDTO> frequencyControlMap) {
        // Redis.incr方法里面的lua脚本执行，增加键的值，并设置过期时间。
        // RedisUtils.incr方法有解析的问题，这里需要确保传入的键值对已经存在，并且已经设置过期时间。
        frequencyControlMap.forEach((k, v) -> RedisUtils.incr(k, v.getTime(), v.getUnit()));
    }

    // 检查是否达到速率限制
    // 解释：该方法通过比较当前计数和限制计数来判断是否达到速率限制
    @Override
    protected boolean reachRateLimit(Map<String, FrequencyControlDTO> frequencyControlMap) {
        // 排序，根据键值对中的计数(最大访问次数)进行排序
        frequencyControlMap = frequencyControlMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(FrequencyControlDTO::getCount)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing, // 解决键冲突
                        LinkedHashMap::new));
        ArrayList<String> frequencyKeys = new ArrayList<>(frequencyControlMap.keySet());
        List<Integer> countList = RedisUtils.mget(frequencyKeys, Integer.class);
        for (int i = 0; i < frequencyKeys.size(); i++) {
            String key = frequencyKeys.get(i);
            Integer count = countList.get(i);
            Integer frequencyControlCount = frequencyControlMap.get(key).getCount();
            if (Objects.nonNull(count) && count >= frequencyControlCount) {
                log.warn("frequencyControl limit key:{},count:{}", key, count);
                return true;
            }
        }
        return false;
    }

}

