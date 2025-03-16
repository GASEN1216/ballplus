package com.gasen.usercenterbackend.model.dto;

import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.dao.SubComment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UpdateSubComment implements Serializable {
    private static final long serialVersionUID = 1L;

    // 要修改的评论ID
    private Long subCommentId;
    // 当前操作用户ID，用于权限校验
    private Integer userId;
    // 修改后的评论内容
    private String content;

    public SubComment toSubComment() {
        SubComment subComment = new SubComment();
        subComment.setId(subCommentId);
        subComment.setAppId(userId);
        subComment.setContent(content);
        return subComment;
    }
}
