package com.gasen.usercenterbackend.model.dto;

import com.gasen.usercenterbackend.model.dao.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UpdateComment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 要修改的评论ID
    private Long commentId;
    // 当前操作用户ID，用于权限校验
    private Long userId;
    // 修改后的评论内容
    private String content;

    public Comment toComment() {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setAppId(userId);
        comment.setContent(content);
        return comment;
    }
}
