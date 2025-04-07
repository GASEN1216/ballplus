package com.gasen.usercenterbackend.model.dto;

import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.dao.SubComment;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddSubComment implements Serializable {
    private static final long serialVersionUID = 1L;

    // 发表评论时，使用者的ID
    private Long userId;
    // 使用者姓名
    private String appName;
    // 使用者头像
    private String avatar;
    // 使用者等级
    private Integer grade;
    // 所属评论ID
    private Long commentId;
    // 评论内容
    private String content;

    public SubComment toSubComment() {
        SubComment subComment = new SubComment();
        subComment.setAppId(userId);
        subComment.setAvatar(avatar);
        subComment.setCommentId(commentId);
        subComment.setAppName(appName);
        subComment.setGrade(grade);
        subComment.setContent(content);
        return subComment;
    }
}
