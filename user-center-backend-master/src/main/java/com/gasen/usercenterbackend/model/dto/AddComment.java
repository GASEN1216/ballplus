package com.gasen.usercenterbackend.model.dto;

import com.gasen.usercenterbackend.model.dao.Comment;
import lombok.Data;

import java.io.Serializable;

@Data
public class AddComment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 发表评论时，使用者的ID
    private Long userId;
    // 使用者姓名
    private String appName;
    // 使用者头像
    private String avatar;
    // 使用者等级
    private Integer grade;
    // 所属帖子ID
    private Long postId;
    // 评论内容
    private String content;

    public Comment toComment() {
        Comment comment = new Comment();
        comment.setAppId(userId);
        comment.setAvatar(avatar);
        comment.setPostId(postId);
        comment.setAppName(appName);
        comment.setGrade(grade);
        comment.setContent(content);
        return comment;
    }
}
