package com.hs.mallchat.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;

import lombok.*;

/**
 * <p>
 * 用户表情包
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-08-08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("user_emoji")
public class UserEmoji implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户表ID
     */
    @TableField("uid")
    private Long uid;

    /**
     * 表情地址
     */
    @TableField("expression_url")
    private String expressionUrl;

    /**
     * 逻辑删除(0-正常,1-删除)
     */
    @TableField("delete_status")
    @TableLogic(value = "0", delval = "1")
    private Integer deleteStatus;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private Date updateTime;


}
