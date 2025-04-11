package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 资源视图对象
 */
@Data
public class ResourceVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 资源类型：video/article
     */
    private String type;

    /**
     * 资源内容
     */
    private String content;

    /**
     * 浏览量
     */
    private Integer views;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 分类
     */
    private String category;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否已收藏
     */
    private Boolean isFavorite;

    private static final long serialVersionUID = 1L;
} 