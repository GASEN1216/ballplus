package com.gasen.usercenterbackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 信誉分变动记录实体类
 */
@TableName(value = "credit_record")
@Data
public class CreditRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 信誉分变动值（正数为增加，负数为减少）
     */
    private Integer creditChange;
    
    /**
     * 变动原因类型：
     * 1-投诉扣分
     * 2-取消活动扣分
     * 3-退出活动扣分
     * 4-管理员调整
     * 5-其他原因
     */
    private Integer changeType;
    
    /**
     * 详细原因
     */
    private String changeReason;
    
    /**
     * 关联记录ID（如投诉ID、活动ID等）
     */
    private Long relationId;
    
    /**
     * 记录创建时间
     */
    private Date createTime;
    
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 