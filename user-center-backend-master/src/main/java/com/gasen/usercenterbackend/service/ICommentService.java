package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.dto.AddComment;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.model.vo.CommentInfo;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;

import java.util.List;

public interface ICommentService extends ILikesService {
    /**
     * 添加评论
     * 
     * @param addComment 评论信息
     * @return 新增评论的ID，失败返回-1
     */
    Long addComment(AddComment addComment);

    boolean deleteComment(Long commentId, Long userId);

    boolean updateComment(UpdateComment updateComment);

    List<Comment> getCommentListByPost(Long postId);

    Comment getComment(Long commentId);

    List<CommentDetail> getCommentsByPostId(Long postId);

    Integer getLikesById(Long id);

    boolean updateLikes(Long id, Integer likes);

    boolean addComments(Long commentId);

    boolean reduceComments(Long commentId);

    /**
     * 获取指定用户发布的帖子收到的评论列表（传统分页）
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论信息列表
     * @deprecated 使用 getCommentsByPostUserIdWithCursor 代替
     */
    @Deprecated
    List<CommentInfo> getCommentsByPostUserId(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 使用游标分页获取指定用户发布的帖子收到的评论列表
     * 
     * @param userId 用户ID
     * @param cursorRequest 游标分页请求
     * @return 游标分页响应
     */
    CursorPageResponse<CommentInfo> getCommentsByPostUserIdWithCursor(Long userId, CursorPageRequest cursorRequest);
}
