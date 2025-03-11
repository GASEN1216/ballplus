package com.gasen.usercenterbackend.model.Request;

import com.gasen.usercenterbackend.model.dao.Post;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddPost implements Serializable {
    // 用于新增帖子的 appId（或参与者 ID）
    private Integer addId;
    private String appName;
    private String avatar;
    private Integer grade;
    private String title;
    private String content;
    private String picture;

    public Post toPost() {
        return new Post()
                .setAppId(addId)
                .setAppName(appName)
                .setAvatar(avatar)
                .setGrade(grade)
                .setTitle(title)
                .setContent(content)
                .setPicture(picture);
    }
}
