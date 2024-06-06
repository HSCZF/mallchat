package com.hs.mallchat.common.common.domain.vo.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 10:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("基础翻页返回")
public class PageBaseResp<T> {

    @ApiModelProperty("当前页数")
    private Integer pageNo;

    @ApiModelProperty("每页查询数量")
    private Integer pageSize;

    @ApiModelProperty("总记录数")
    private Long totalRecords;

    @ApiModelProperty("是否最后一页")
    private Boolean isLast = Boolean.FALSE;

    @ApiModelProperty("数据列表")
    private List<T> list;

    /**
     * 创建一个空的分页响应对象
     *
     * @param <T> 泛型类型
     * @return 分页响应对象
     */
    public static <T> PageBaseResp<T> empty() {
        PageBaseResp<T> r = new PageBaseResp<>();
        r.setPageNo(1);
        r.setPageSize(0);
        r.setIsLast(true);
        r.setTotalRecords(0L);
        r.setList(new ArrayList<>());
        return r;
    }

    /**
     * 初始化分页响应对象
     *
     * @param pageNo       当前页数
     * @param pageSize     每页大小
     * @param totalRecords 总记录数
     * @param isLast       是否最后一页
     * @param list         数据列表
     * @param <T>          泛型类型
     * @return 分页响应对象
     */
    public static <T> PageBaseResp<T> init(Integer pageNo, Integer pageSize, Long totalRecords, Boolean isLast, List<T> list) {
        return new PageBaseResp<>(pageNo, pageSize, totalRecords, isLast, list);
    }

    /**
     * 初始化分页响应对象（简化版本，根据总记录数计算是否最后一页）
     *
     * @param pageNo       当前页数
     * @param pageSize     每页大小
     * @param totalRecords 总记录数
     * @param list         数据列表
     * @param <T>          泛型类型
     * @return 分页响应对象
     */
    public static <T> PageBaseResp<T> init(Integer pageNo, Integer pageSize, Long totalRecords, List<T> list) {
        return new PageBaseResp<>(pageNo, pageSize, totalRecords, isLastPage(totalRecords, pageNo, pageSize), list);
    }

    /**
     * 根据Mybatis Plus的IPage对象初始化分页响应对象
     *
     * @param page Mybatis Plus的IPage对象
     * @param <T>  泛型类型
     * @return 分页响应对象
     */
    public static <T> PageBaseResp<T> init(IPage<T> page) {
        return init((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), page.getRecords());
    }

    /**
     * 根据Mybatis Plus的IPage对象和自定义数据列表初始化分页响应对象
     *
     * @param page Mybatis Plus的IPage对象
     * @param list 自定义数据列表
     * @param <T>  泛型类型
     * @return 分页响应对象
     */
    public static <T> PageBaseResp<T> init(IPage page, List<T> list) {
        return init((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), list);
    }

    /**
     * 根据已有PageBaseResp对象和新数据列表初始化分页响应对象
     *
     * @param resp 原始的PageBaseResp对象
     * @param list 新的数据列表
     * @param <T>  泛型类型
     * @return 分页响应对象
     */
    public static <T> PageBaseResp<T> init(PageBaseResp resp, List<T> list) {
        return init(resp.getPageNo(), resp.getPageSize(), resp.getTotalRecords(), resp.getIsLast(), list);
    }

    /**
     * 判断是否为最后一页
     *
     * @param totalRecords 总记录数
     * @param pageNo       当前页数
     * @param pageSize     每页大小
     * @return 是否为最后一页
     */
    public static Boolean isLastPage(long totalRecords, int pageNo, int pageSize) {
        if (pageSize == 0) {
            return false;
        }
        long pageTotal = totalRecords / pageSize + (totalRecords % pageSize == 0 ? 0 : 1);
        return pageNo >= pageTotal ? true : false;
    }


}
