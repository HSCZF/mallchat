package com.hs.mallchat.common.common.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.hs.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.hs.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @Author: CZF
 * @Create: 2024/6/24 - 9:45
 * Description: 游标分页工具类，提供基于Mybatis Plus的游标分页查询支持。
 * 游标分页是一种高效的分页方式，尤其适用于大数据量的分页查询，通过比较上一次查询结果的最后一个字段值来获取下一页的数据。
 */
public class CursorUtils {

    /**
     * 根据游标和请求参数，执行游标分页查询，并返回查询结果。
     *
     * @param mapper        Mybatis Plus的IService接口实现，用于执行查询。
     * @param request       分页请求参数，包含游标信息和分页条件。
     * @param initWrapper   查询条件初始化回调，用于进一步定制查询条件。
     * @param cursorColumn  游标字段，用于指定按照哪个字段进行游标比较。
     * @param <T>           数据实体类型。
     * @return 游标分页响应对象，包含查询结果、游标和是否为最后一页的信息。
     */
    public static <T> CursorPageBaseResp<T> getCursorPageByMysql(IService<T> mapper,
                                                                        CursorPageBaseReq request,
                                                                        Consumer<LambdaQueryWrapper<T>> initWrapper,
                                                                        SFunction<T, ?> cursorColumn) {
        // 游标字段的类型
        Class<?> cursorType = LambdaUtils.getReturnType(cursorColumn);
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        // 初始化查询条件
        initWrapper.accept(wrapper);

        // 根据游标设置查询条件，如果游标存在
        if (StrUtil.isNotBlank(request.getCursor())) {
            wrapper.lt(cursorColumn, parseCursor(request.getCursor(), cursorType));
        }
        // 按照游标字段降序排序
        wrapper.orderByDesc(cursorColumn);

        // 执行分页查询
        Page<T> page = mapper.page(request.plusPage(), wrapper);
        // 计算新的游标值
        String cursor = Optional.ofNullable(CollectionUtil.getLast(page.getRecords()))
                .map(cursorColumn)
                .map(CursorUtils::toCursor)
                .orElse(null);
        // 判断是否为最后一页
        Boolean isLast = page.getRecords().size() != request.getPageSize();
        // 返回分页响应对象
        return new CursorPageBaseResp<>(cursor, isLast, page.getRecords());
    }

    /**
     * 将字段值转换为游标字符串。
     *
     * @param o 字段值对象。
     * @return 游标字符串。
     */
    private static String toCursor(Object o) {
        // 对于Date类型，转换为时间戳字符串
        if (o instanceof Date) {
            return String.valueOf(((Date) o).getTime());
        } else {
            // 其他类型直接转换为字符串
            return o.toString();
        }
    }

    /**
     * 将游标字符串解析为对应的字段值对象。
     *
     * @param cursor      游标字符串。
     * @param cursorClass 字段值的类类型。
     * @return 解析后的字段值对象。
     */
    private static Object parseCursor(String cursor, Class<?> cursorClass) {
        // 如果是Date类型，解析为Date对象
        if (Date.class.isAssignableFrom(cursorClass)) {
            return new Date(Long.parseLong(cursor));
        } else {
            // 其他类型直接转换为字符串
            return cursor;
        }
    }
}
