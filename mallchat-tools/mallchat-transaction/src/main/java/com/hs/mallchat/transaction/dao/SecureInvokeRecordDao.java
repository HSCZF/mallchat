package com.hs.mallchat.transaction.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.hs.mallchat.transaction.domain.entity.SecureInvokeRecord;
import com.hs.mallchat.transaction.mapper.SecureInvokeRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.mallchat.transaction.service.SecureInvokeService;
import com.sun.org.apache.xml.internal.security.Init;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 本地消息表 服务实现类
 * DAO层实现，用于处理安全调用记录的数据库操作。
 *
 * @author czf
 * @since 2024-07-24
 */
@Component
public class SecureInvokeRecordDao extends ServiceImpl<SecureInvokeRecordMapper, SecureInvokeRecord> {

    /**
     * 查询等待重试的安全调用记录。
     * 此方法用于查找所有状态为等待重试（STATUS_WAIT）并且下次重试时间小于当前时间，
     * 同时创建时间早于两分钟前的记录。目的是避免刚插入数据库的记录被立即重试。
     *
     * @return 包含满足条件的安全调用记录的列表。
     */
    public List<SecureInvokeRecord> getWaitRetryRecords() {
        Date now = new Date();
        // 计算两分钟前的时间点，用于过滤刚创建的记录。
        DateTime afterTime = DateUtil.offsetMinute(now, -(int) SecureInvokeService.RETRY_INTERVAL_MINUTES);

        // 使用Lambda查询方式构建查询条件，执行查询并返回结果列表。
        return lambdaQuery()
                .eq(SecureInvokeRecord::getStatus, SecureInvokeRecord.STATUS_WAIT) // 状态为等待重试
                .lt(SecureInvokeRecord::getNextRetryTime, new Date()) // 下次重试时间小于当前时间
                .lt(SecureInvokeRecord::getCreateTime, afterTime) // 创建时间早于两分钟前
                .list(); // 执行查询并返回记录列表
    }
}

