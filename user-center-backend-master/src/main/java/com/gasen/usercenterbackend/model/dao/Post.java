package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.*;
import com.gasen.usercenterbackend.model.respond.PostDetail;
import com.gasen.usercenterbackend.model.respond.PostInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("post")
public class Post {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String appName;
    private String avatar;
    private Integer grade;
    private String title;
    private String content;
    private Integer likes;
    private Integer comments;
    private String picture;

    @TableLogic(value = "0", delval = "1")
    private Byte isDelete;

    private LocalDateTime createTime;

    @TableField(update = "now()")
    private LocalDateTime updateTime;

    public PostDetail toPostDetail() {
        return new PostDetail()
                .setId(this.id)
                .setAppId(this.appId)
                .setAvatar(this.avatar)
                .setAppName(this.appName)
                .setGrade(this.grade)
                .setTitle(this.title)
                .setContent(this.content)
                .setLikes(this.likes)
                .setComments(this.comments)
                .setPicture(this.picture)
                .setCreateTime(this.createTime)
                .setUpdateTime(this.updateTime);
    }

    public PostInfo toPostInfo() {
        return new PostInfo()
                .setPostId(this.id)
                .setAppId(this.appId)
                .setAppName(this.appName)
                .setAvatar(this.avatar)
                .setGrade(this.grade)
                .setTitle(this.title)
                .setContent(this.content)
                .setLikes(this.likes)
                .setComments(this.comments)
                .setPicture(this.picture);
    }
}
