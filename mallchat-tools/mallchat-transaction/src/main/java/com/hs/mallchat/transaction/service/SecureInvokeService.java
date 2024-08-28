package com.hs.mallchat.transaction.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.hs.mallchat.transaction.dao.SecureInvokeRecordDao;
import com.hs.mallchat.transaction.domain.dto.SecureInvokeDTO;
import com.hs.mallchat.transaction.domain.entity.SecureInvokeRecord;
import com.hs.mallchat.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Description: 安全执行处理器
 * SecureInvokeService 类提供安全调用服务，管理安全调用记录，支持重试机制。
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 11:03
 */
@Slf4j
@AllArgsConstructor
public class SecureInvokeService {

    // 定义重试间隔的最小单位为2分钟
    public static final double RETRY_INTERVAL_MINUTES = 2D;

    // 注入安全调用记录DAO和Executor实例
    private final SecureInvokeRecordDao secureInvokeRecordDao;
    private final Executor executor;

    // 定时任务，每5秒执行一次，获取并重试所有等待重试的记录
    @Scheduled(cron = "*/5 * * * * ?")
    public void retry() {
        // 获取所有待重试的记录
        List<SecureInvokeRecord> secureInvokeRecords = secureInvokeRecordDao.getWaitRetryRecords();
        // 遍历记录，逐个异步执行
        for (SecureInvokeRecord secureInvokeRecord : secureInvokeRecords) {
            doAsyncInvoke(secureInvokeRecord);
        }
    }

    // 保存安全调用记录
    public void save(SecureInvokeRecord record) {
        secureInvokeRecordDao.save(record);
    }

    // 处理重试记录，更新状态和重试时间
    private void retryRecord(SecureInvokeRecord record, String errorMsg) {
        // 增加重试次数
        Integer retryTimes = record.getRetryTimes() + 1;
        // 创建更新记录
        SecureInvokeRecord update = new SecureInvokeRecord();
        update.setId(record.getId());
        update.setFailReason(errorMsg);
        update.setNextRetryTime(getNextRetryTime(retryTimes));
        // 判断是否达到最大重试次数
        if (retryTimes > record.getMaxRetryTimes()) {
            update.setStatus(SecureInvokeRecord.STATUS_FAIL);
        } else {
            update.setRetryTimes(retryTimes);
        }
        // 更新记录
        secureInvokeRecordDao.updateById(update);
    }

    // 计算下一次重试时间，基于指数退避策略
    private Date getNextRetryTime(Integer retryTimes) {
        // 计算等待时间（分钟），随重试次数增加而指数增长
        double waitMinutes = Math.pow(RETRY_INTERVAL_MINUTES, retryTimes);
        // 返回计算后的Date对象
        return DateUtil.offsetMinute(new Date(), (int) waitMinutes);
    }

    // 删除指定ID的记录
    private void removeRecord(Long id) {
        secureInvokeRecordDao.removeById(id);
    }

    // 在事务中调用安全方法，支持异步选项
    public void invoke1(SecureInvokeRecord record, boolean async) {
        // 检查当前是否有活跃的事务
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        // 若无事务，则直接返回，不作任何处理
        if (!inTransaction) {
            return;
        }
        // 保存记录
        save(record);
        // 注册事务同步器，事务提交后执行
        /**
         * 创建了一个匿名内部类TransactionSynchronization并实现了afterCommit()方法。
         * 这个方法将在事务成功提交后被调用。以下是具体的工作流程：
         * 1、事务开始：当一个事务开始时，Spring会跟踪这个事务，并在事务管理上下文中记录相关信息。
         * 2、调用invoke()方法：在事务的某个阶段，invoke()方法被调用。如果此时存在一个活动的事务，invoke()方法将继续执行。
         * 3、保存记录：invoke()方法中调用了save(record)，这通常涉及数据库操作，保存SecureInvokeRecord对象。
         * 4、注册同步器：接下来，registerSynchronization()被调用，注册了一个TransactionSynchronization实例。但是，此时afterCommit()方法并不会立即执行，因为事务尚未提交。
         * 5、事务提交：在事务的其他部分完成并提交后，Spring事务管理器会遍历所有已注册的TransactionSynchronization实例，并调用它们的afterCommit()方法。
         * 6、执行回调：这时，afterCommit()方法被调用，根据async参数的值，选择调用doAsyncInvoke(record)或doInvoke(record)。
         * 7、当事务提交后，确实会“跳回”到之前注册的TransactionSynchronization实例的afterCommit()方法中执行，
         **/
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            //   第一次是有事务的，所以第一次不会走下面的逻辑,先去到别的地方，然后在返回这里
            @SneakyThrows
            @Override
            public void afterCommit() {
                // 根据异步选项，选择执行方式
                if (async) {
                    doAsyncInvoke(record);
                } else {
                    doInvoke(record);
                }
            }
        });
    }

    // 异步执行安全调用记录的方法调用
    public void doAsyncInvoke(SecureInvokeRecord record) {
        // 提交任务到Executor执行
        executor.execute(() -> {
            // 打印线程名称
            System.out.println("打印线程名称：" + Thread.currentThread().getName());
            // 执行方法调用
            doInvoke(record);
        });
    }

    // 同步执行安全调用记录的方法调用
    public void doInvoke(SecureInvokeRecord record) {
        // 获取调用详情
        SecureInvokeDTO secureInvokeDTO = record.getSecureInvokeDTO();
        try {
            // 设置正在调用标志
            SecureInvokeHolder.setInvoking();
            // 反射获取并调用目标方法
            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass);
            List<String> parameterStrings = JsonUtils.toList(secureInvokeDTO.getParameterTypes(), String.class);
            List<Class<?>> parameterClasses = getParameters(parameterStrings);
            Method method = ReflectUtil.getMethod(beanClass, secureInvokeDTO.getMethodName(), parameterClasses.toArray(new Class[]{}));
            Object[] args = getArgs(secureInvokeDTO, parameterClasses);
            // 执行方法
            // method.invoke(bean, args)反射后拿到的invoke， 被切面拦截到了，又回到了around那里去了
            method.invoke(bean, args);
            // 成功后删除记录
            removeRecord(record.getId());
        } catch (Throwable e) {
            // 记录错误日志
            log.error("SecureInvokeService invoke fail", e);
            // 失败后重试记录
            retryRecord(record, e.getMessage());
        } finally {
            // 清除调用标志
            SecureInvokeHolder.invoked();
        }
    }

    // 将JSON参数转换为目标方法参数类型
    @NotNull
    private Object[] getArgs(SecureInvokeDTO secureInvokeDTO, List<Class<?>> parameterClasses) {
        // 解析参数JSON，转换为对应Java对象数组
        JsonNode jsonNode = JsonUtils.toJsonNode(secureInvokeDTO.getArgs());
        Object[] args = new Object[jsonNode.size()];
        for (int i = 0; i < jsonNode.size(); i++) {
            Class<?> aClass = parameterClasses.get(i);
            args[i] = JsonUtils.nodeToValue(jsonNode.get(i), aClass);
        }
        return args;
    }

    // 将字符串参数类型转换为Class对象列表
    @NotNull
    private List<Class<?>> getParameters(List<String> parameterStrings) {
        // 转换并收集参数类型
        return parameterStrings.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                // 记录错误日志
                log.error("SecureInvokeService class not fund", e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
