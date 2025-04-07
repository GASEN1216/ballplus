package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SubCommentInfo {
    // 子评论ID
    private Long subCommentId;
    // 父评论ID
    private Long commentId;
    // 父评论发布者ID
    private Long parentCommentAppId;
    // 用户名
    private String appName;
    // 用户ID
    private Long appId;
    // 用户头像
    private String avatar;
    // 用户等级
    private Integer grade;
    // 子评论内容
    private String content;
    // 父评论内容
    private String originalCommentContent;
    // 点赞数
    private Integer likes;
    // 创建时间
    private LocalDateTime createTime;
} 