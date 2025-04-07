package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

/**
 * 资源查询请求
 */
@Data
public class ResourceQueryRequest {
    /**
     * 类型
     */
    private Integer type;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类
     */
    private String category;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;
} 