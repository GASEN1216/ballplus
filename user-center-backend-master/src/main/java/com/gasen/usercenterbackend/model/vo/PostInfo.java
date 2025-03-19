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
public class PostInfo implements Serializable {
    // 帖子列表粗略信息：帖子ID、appId、头像、截取后的内容、图片、点赞数、评论数
    private Long postId;
    private Integer appId;
    private String appName;
    private String avatar;
    private Integer grade;
    private String title;
    private String content;
    private String picture;
    private int likes;
    private int comments;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
