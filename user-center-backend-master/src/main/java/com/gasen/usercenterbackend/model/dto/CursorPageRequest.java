package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

/**
 * 游标分页请求参数
 */
@Data
public class CursorPageRequest {
    /**
     * 游标值（第一页可为空）
     */
    private String cursor;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 是否升序（默认为false，表示降序）
     */
    private Boolean asc = false;
} 