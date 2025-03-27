package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 投诉记录表
 * </p>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("complaint")
public class Complaint implements Serializable {

    @Serial
    @TableField(exist=false)
    private static final long serialVersionUID = 1L;

    /**
     * 投诉ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    private Long eventId;

    /**
     * 投诉人ID
     */
    private Integer complainerId;

    /**
     * 被投诉人ID
     */
    private Integer complainedId;

    /**
     * 投诉内容
     */
    private String content;

    /**
     * 状态 0-待处理 1-有效 2-无效
     */
    private Byte status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(update = "now()")
    private LocalDateTime updateTime;
} 