package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动取消记录表
 */
@Data
@TableName("event_cancel_record")
public class EventCancelRecord {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 活动ID
     */
    private Long eventId;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 