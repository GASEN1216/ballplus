package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.Request.AddComment;
import com.gasen.usercenterbackend.model.Request.CommentDetail;
import com.gasen.usercenterbackend.model.Request.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;

import java.util.List;

public interface ICommentService {
    boolean addComment(AddComment addComment);

    boolean deleteComment(Long commentId, Integer userId);

    boolean updateComment(UpdateComment updateComment);

    List<Comment> getCommentListByPost(Long postId);

    Comment getComment(Long commentId);

    List<CommentDetail> getCommentsByPostId(Long postId);
}
