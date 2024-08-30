package com.hs.mallchat.common.common.service.frequencycontrol;

import com.hs.mallchat.common.common.domain.dto.FrequencyControlDTO;
import com.hs.mallchat.common.common.exception.CommonErrorEnum;
import com.hs.mallchat.common.common.exception.FrequencyControlException;
import com.hs.mallchat.common.common.utils.AssertUtil;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 * 抽象类频控服务，提供频率控制的基础设施。
 * 其他类如果要实现限流服务，直接注入使用通用限流类
 * 子类需要提供具体的频率控制策略实现
 * 后期会在mallchat-frequency-control模块下开发 --》通过继承此类实现令牌桶等算法
 *
 * @param <K> FrequencyControlDTO的子类，用于具体的频率控制数据传输对象
 * @Author: CZF
 * @Create: 2024/8/30 - 9:20
 * /**
 * 抽象频率控制服务类，提供频率控制的基础设施
 * 子类需要提供具体的频率控制策略实现
 */
public abstract class AbstractFrequencyControlService<K extends FrequencyControlDTO> {

    /**
     * 获取策略名称
     *
     * @return 频率控制策略的名称
     */
    protected abstract String getStrategyName();

    /**
     * 判断是否达到速率限制
     *
     * @param frequencyControlMap 频率控制映射，包含频率控制信息
     * @return 如果达到速率限制则返回true，否则返回false
     */
    protected abstract boolean reachRateLimit(Map<String, K> frequencyControlMap);

    /**
     * 增加频率控制统计计数
     *
     * @param frequencyControlMap 频率控制映射，包含频率控制信息
     */
    protected abstract void addFrequencyControlStatisticsCount(Map<String, K> frequencyControlMap);

    /**
     * 在构造后注册当前实例到工厂
     * <p>
     * 此方法会在 Spring 容器初始化完成后自动调用，将当前实例注册到频率控制策略工厂中。
     * 这样做的目的是确保当前策略实例能够在应用程序启动时正确地注册到工厂，
     * 以便后续可以通过工厂获取并使用该策略实例。
     * this指向的是当前类（AbstractFrequencyControlService）的对象
     * 由于 AbstractFrequencyControlService 是一个抽象类，实际创建的对象应该是它的某个子类（例如 TotalCountWithInFixTimeFrequencyController
     * getStrategyName() 返回子类的具体策略名称。
     * 将会放进限流的集合里面，方便使用
     */
    @PostConstruct
    protected void registerMyselfToFactory() {
        FrequencyControlStrategyFactory.registerFrequencyController(getStrategyName(), this);
    }


    // 执行频率控制逻辑
    // -------------------------------------------------------------------------------------------------------------------
    // executeWithFrequencyControl -> executeWithFrequencyControlList -> executeWithFrequencyControlMap


    /**
     * 使用频率控制执行给定的供应商逻辑
     *
     * @param frequencyControl 频率控制DTO，包含频率控制的信息
     * @param supplier         要执行的供应商逻辑
     * @param <T>              返回值的类型
     * @return 执行结果
     * @throws Throwable 如果执行过程中发生错误
     */
    public <T> T executeWithFrequencyControl(K frequencyControl, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        // 将单个频率控制转换为列表，然后调用 executeWithFrequencyControlList 方法
        return executeWithFrequencyControlList(Collections.singletonList(frequencyControl), supplier);
    }

    /**
     * 使用频率控制 列表 执行给定的供应商逻辑
     * <@SuppressWarnings("unchecked")>：来抑制编译器的未检查警告。这通常用于处理泛型相关的警告。
     *
     * @param frequencyControlList 频率控制DTO列表，包含频率控制的信息
     * @param supplier             要执行的供应商逻辑
     * @param <T>                  返回值的类型
     * @return 执行结果
     * @throws Throwable 如果执行过程中发生错误
     */
    @SuppressWarnings("unchecked")
    public <T> T executeWithFrequencyControlList(List<K> frequencyControlList, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        // .anyMatch：一个谓词（即一个返回布尔值的函数），用于判断每个元素是否满足某个条件。
        // 如果存在一个元素满足该条件，则直接返回 true，否则遍历完都没有满足的就返回 false。
        boolean existsFrequencyControlHasNullKey = frequencyControlList.stream()
                .anyMatch(frequencyControl -> ObjectUtils.isEmpty(frequencyControl.getKey()));
        AssertUtil.isFalse(existsFrequencyControlHasNullKey, "限流策略的Key字段不允许出现空值");
        // 1、将frequencyControlList 转换成一个 Stream<FrequencyControlDTO>。
        // 2、将Stream<FrequencyControlDTO> 分组为 Map<String, List<FrequencyControlDTO>>
        // 3、对每个分组的 List 进行处理，取出第一个元素，结果是一个 Map<String, FrequencyControlDTO>。
        Map<String, FrequencyControlDTO> frequencyControlDtoMap = frequencyControlList.stream()
                .collect(Collectors.groupingBy(FrequencyControlDTO::getKey, Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
        return executeWithFrequencyControlMap((Map<String, K>) frequencyControlDtoMap, supplier);
    }

    /**
     * 私有方法，使用频率控制 映射 执行给定的供应商逻辑
     *
     * @param frequencyControlMap 频率控制映射，包含频率控制信息
     * @param supplier            要执行的供应商逻辑
     * @param <T>                 返回值的类型
     * @return 执行结果
     * @throws Throwable 如果执行过程中发生错误
     */
    private <T> T executeWithFrequencyControlMap(Map<String, K> frequencyControlMap, SupplierThrowWithoutParam<T> supplier) throws Throwable {
        if (reachRateLimit(frequencyControlMap)) {
            throw new FrequencyControlException(CommonErrorEnum.FREQUENCY_LIMIT);
        }
        try {
            return supplier.get();
        } finally {
            // 不管成功还是失败，都增加次数，此方法用于在频率控制映射中为每个键值对增加计数
            // Redis 脚本lua执行：incr 方法用于在 Redis 中增加键的值并设置过期时间。
            // 会对每个方法上的方法注解进行增加，比如发消息方法上有3个@FrequencyControl(time = 5, count = 3, target = FrequencyControl.Target.UID)注解
            // 那就frequencyControlMap键值对就有3个，就增加3个，比如当前这个，UID,3个频控注解，第一次都是 1,1,1
            // 每发一次消息就加1，当前第一个注解5秒内发3次，在5秒内超过3次就会被上面的reachRateLimit(frequencyControlMap)抓到，直至时间过期。
            // 假设第二个注解为30秒，10次，刚才5秒的频控过去了，然后继续狂输入10次，又被频控了，直至时间过期
            // 5秒内发了2次，那么这个redis的时间过期就被删除，又重新开始计数
            addFrequencyControlStatisticsCount(frequencyControlMap);
        }
    }

    /**
     * 无参数的供应商接口，允许抛出异常
     * <@FunctionalInterface> 是一个 Java 注解，用于标记一个接口为 函数式接口。
     * 单抽象方法：函数式接口只能有一个抽象方法。
     * 默认方法和静态方法：可以包含任意数量的默认方法和静态方法。
     * 编译器检查：使用此注解可以帮助编译器检查接口是否确实只有一个抽象方法。如果违反此规则，编译器将抛出错误。
     *
     * @param <T> 返回值的类型
     */
    @FunctionalInterface
    public interface SupplierThrowWithoutParam<T> {
        T get() throws Throwable;
    }

    /**
     * 执行器接口，允许抛出异常
     */
    @FunctionalInterface
    public interface Executor {
        void execute() throws Throwable;
    }

}
