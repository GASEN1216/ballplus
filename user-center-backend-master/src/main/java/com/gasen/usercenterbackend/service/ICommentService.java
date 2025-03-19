package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dto.AddComment;
import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.model.vo.CommentInfo;
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

    /**
     * 获取指定用户发布的帖子收到的评论列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论信息列表
     */
    List<CommentInfo> getCommentsByPostUserId(Integer userId, Integer pageNum, Integer pageSize);
}
