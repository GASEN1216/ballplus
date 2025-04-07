package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户信誉分记录表
 */
@Data
@Accessors(chain = true)
@TableName("credit_record")
public class CreditRecord {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 变动原因类型
     * 1: 投诉扣分
     * 2: 取消活动扣分
     * 3: 退出活动扣分
     * 4: 管理员调整
     * 5: 其他原因
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
     * 创建时间
     */
    private LocalDateTime createTime;
} 