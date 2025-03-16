package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.CommentMapper;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.dto.AddComment;

import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.service.ICommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CommentServiceImpl implements ICommentService {
    @Resource
    private CommentMapper commentMapper;

    @Override
    public boolean addComment(AddComment addComment) {
        try {
            Comment comment = addComment.toComment();
            int insert = commentMapper.insert(comment);
            return insert > 0;
        } catch (Exception e) {
            log.error("添加评论异常", e);
            return false;
        }
    }

    @Override
    public boolean deleteComment(Long commentId, Integer userId) {
        // 先查找评论，再看userId是否本人,最后逻辑删除
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null && comment.getAppId().equals(userId)) {
            comment.setIsDelete((byte) 1);
            int update = commentMapper.updateById(comment);
            return update > 0;
        } else {
            return false;
        }

    }

    @Override
    public boolean updateComment(UpdateComment updateComment) {
        try {
            // 先查找评论，再看userId是否本人,最后修改评论
            Comment comment = commentMapper.selectById(updateComment.getCommentId());
            if (comment != null && comment.getAppId().equals(updateComment.getUserId())) {
                comment.setContent(updateComment.getContent());
            } else {
                log.error("修改评论异常，评论不存在或评论用户id不匹配");
                return false;
            }
            int update = commentMapper.updateById(comment);
            return update > 0;
        } catch (Exception e) {
            log.error("修改评论异常", e);
            return false;
        }
    }

    @Override
    public List<Comment> getCommentListByPost(Long postId) {
        try {
            return commentMapper.selectList(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        } catch (Exception e) {
            log.error("查询评论列表异常", e);
            return null;
        }
    }

    @Override
    public Comment getComment(Long commentId) {
        return commentMapper.selectById(commentId);
    }

    @Override
    public List<CommentDetail> getCommentsByPostId(Long postId) {
        // 查询所有post_id == postId的评论
        List<Comment> comments = commentMapper
                .selectList(new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, postId));
        if (comments == null) {
            return new ArrayList<>();
        }
        return comments.stream().map(Comment::toCommentDetail).toList();
    }

    @Override
    public Integer getLikesById(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "评论不存在");
        return comment.getLikes();
    }

    @Override
    public boolean updateLikes(Long id, Integer likes) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "帖子不存在");
        }
        if (likes == null || likes < 0)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞数错误");
        comment.setLikes(likes);
        return commentMapper.updateById(comment) > 0;
    }

    @Override
    public boolean addComments(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "评论不存在");
        }
        comment.setComments(comment.getComments() + 1);
        return commentMapper.updateById(comment) > 0;
    }

    @Override
    public boolean reduceComments(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "评论不存在");
        }
        comment.setComments(comment.getComments() - 1);
        return commentMapper.updateById(comment) > 0;
    }
}
