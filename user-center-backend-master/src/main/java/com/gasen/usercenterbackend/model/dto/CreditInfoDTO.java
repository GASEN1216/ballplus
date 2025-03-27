package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

/**
 * 信誉分信息DTO
 */
@Data
public class CreditInfoDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 信誉分
     */
    private Integer credit;
    
    /**
     * 等级名称（前端会根据分数计算，此处预留）
     */
    private String levelName;
    
    /**
     * 等级数字（前端会根据分数计算，此处预留）
     */
    private Integer level;
} 