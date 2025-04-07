package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

/**
 * 游标分页请求包装类，用于接收前端传递的userId和requestData
 */
@Data
public class CursorPageRequestWrapper {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 游标分页请求参数
     */
    private CursorPageRequest requestData;
} 