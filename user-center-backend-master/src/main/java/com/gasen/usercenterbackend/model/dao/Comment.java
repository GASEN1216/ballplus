package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.*;
import com.gasen.usercenterbackend.model.dto.CommentDetail;
import com.gasen.usercenterbackend.model.vo.CommentInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("comment")
public class Comment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String appName;
    private String avatar;
    private Integer grade;
    private Long postId;
    private String content;
    private Integer likes;
    private Integer comments;

    @TableLogic(value = "0", delval = "1")
    private Byte isDelete;

    private LocalDateTime createTime;

    @TableField(update = "now()")
    private LocalDateTime updateTime;

    public CommentInfo toCommentInfo() {
        return new CommentInfo()
                .setCommentId(this.id)
                .setPostId(this.postId)
                .setAppId(this.appId)
                .setAppName(this.appName)
                .setAvatar(this.avatar)
                .setGrade(this.grade)
                .setContent(this.content)
                .setLikes(this.likes)
                .setComments(this.comments)
                .setCreateTime(this.createTime);
    }

    public CommentDetail toCommentDetail() {
        return new CommentDetail()
                .setCommentId(this.id)
                .setPostId(this.postId)
                .setAppId(this.appId)
                .setAppName(this.appName)
                .setAvatar(this.avatar)
                .setGrade(this.grade)
                .setContent(this.content)
                .setLikes(this.likes)
                .setCreateTime(this.createTime);
    }
}
