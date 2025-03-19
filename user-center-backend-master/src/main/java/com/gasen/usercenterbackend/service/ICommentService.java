package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dto.AddComment;
import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import java.util.List;

public interface ICommentService extends ILikesService {
    /**
     * 添加评论
     * 
     * @param addComment 评论信息
     * @return 新增评论的ID，失败返回-1
     */
    Long addComment(AddComment addComment);

    boolean deleteComment(Long commentId, Integer userId);

    boolean updateComment(UpdateComment updateComment);

    List<Comment> getCommentListByPost(Long postId);

    Comment getComment(Long commentId);

    List<CommentDetail> getCommentsByPostId(Long postId);

    boolean addComments(Long commentId);

    boolean reduceComments(Long commentId);
}
