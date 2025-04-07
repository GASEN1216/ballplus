package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CommentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 评论ID
    private Long commentId;
    // 所属帖子ID
    private Long postId;
    // 帖子标题
    private String postTitle;
    // 发布评论的用户ID
    private Long appId;
    // 发布评论的用户名
    private String appName;
    // 用户头像
    private String avatar;
    // 用户等级
    private Integer grade;
    // 评论内容
    private String content;
    // 点赞数
    private int likes;
    // 评论数
    private int comments;
    // 创建时间
    private LocalDateTime createTime;
}
