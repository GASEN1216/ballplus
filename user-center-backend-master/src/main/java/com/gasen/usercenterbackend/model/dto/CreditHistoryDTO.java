package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

/**
 * 信誉分历史记录DTO
 */
@Data
public class CreditHistoryDTO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 变动标题（根据changeType转换）
     */
    private String title;
    
    /**
     * 变动原因
     */
    private String reason;
    
    /**
     * 变动分值（可正可负）
     */
    private Integer change;
    
    /**
     * 记录创建时间
     */
    private String createTime;
} 