package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PostDetail implements Serializable {
    // 帖子详情：appId、头像、标题、内容、点赞数、评论数、图片、创建时间、更新时间
    private Long id;
    private Integer appId;
    private String appName;
    private String avatar;
    private Integer grade;
    private String title;
    private String content;
    private int likes;
    private int comments;
    private String picture;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CommentDetail> commentsList;
}
