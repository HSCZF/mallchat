package com.hs.mallchat.common.common.domain.vo.response;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 10:28
 */
@Data
@ApiModel("游标翻页返回")
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageBaseResp<T> {

    @ApiModelProperty("游标（下次翻页带上这参数）")
    private String cursor;

    @ApiModelProperty("是否最后一页")
    private Boolean isLast = Boolean.FALSE;

    @ApiModelProperty("数据列表")
    private List<T> list;

    /**
     * 根据给定的CursorPageBaseResp实例和数据列表来初始化一个新的CursorPageBaseResp对象。
     *
     * @param cursorPage 原CursorPageBaseResp实例，用于复制isLast和cursor属性。
     * @param list       要设置的数据列表。
     * @param <T>        泛型类型
     * @return 初始化后的CursorPageBaseResp对象。
     */
    public static <T> CursorPageBaseResp<T> init(CursorPageBaseResp<T> cursorPage, List<T> list) {
        CursorPageBaseResp<T> cursorPageBaseResp = new CursorPageBaseResp<T>();
        cursorPageBaseResp.setIsLast(cursorPage.getIsLast());
        cursorPageBaseResp.setList(list);
        cursorPageBaseResp.setCursor(cursorPage.getCursor());
        return cursorPageBaseResp;
    }

    /**
     * 检查当前分页数据是否为空。如果数据列表为空，则认为是空的。
     *
     * @return 如果数据列表为空则返回true，否则返回false。
     */
    @JsonIgnore
    public Boolean isEmpty() {
        return CollectionUtil.isEmpty(list);
    }

    /**
     * 创建一个表示空结果的CursorPageBaseResp实例，其中isLast设为true，表示没有更多数据，list为空列表。
     *
     * @param <T> 泛型类型
     * @return 一个空的CursorPageBaseResp实例。
     */
    public static <T> CursorPageBaseResp<T> empty() {
        CursorPageBaseResp<T> cursorPageBaseResp = new CursorPageBaseResp<T>();
        cursorPageBaseResp.setIsLast(true);
        cursorPageBaseResp.setList(new ArrayList<T>());
        return cursorPageBaseResp;
    }


}
