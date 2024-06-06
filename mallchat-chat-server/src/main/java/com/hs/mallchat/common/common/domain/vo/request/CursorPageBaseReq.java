package com.hs.mallchat.common.common.domain.vo.request;

import cn.hutool.db.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @Author: CZF
 * @Create: 2024/6/6 - 11:20
 */
@Data
@ApiModel("游标翻页请求")
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageBaseReq {

    @ApiModelProperty("页面大小")
    @Min(0)
    @Max(100)
    private Integer pageSize = 10;

    @ApiModelProperty("游标（初始为null，后续请求附带上次翻页的游标）")
    private String cursor;

    /**
     * 构建一个分页对象，用于数据库查询的起始页为第1页，页面大小同本对象配置。
     *
     * @return 一个新的分页对象 {@link Page} 实例。
     */
    public Page plusPage() {
        return new Page(1, this.pageSize);
    }

    /**
     * 判断当前是否为首页请求。通过检查游标是否为空来确定。
     *
     * @return 如果游标为空则表示是首次请求或返回首页，返回true；否则返回false。
     */
    public Boolean isFirstPage() {
        return StringUtils.isEmpty(cursor);
    }


}
