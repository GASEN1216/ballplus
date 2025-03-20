package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.CommentMapper;
import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.dto.AddComment;

import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.model.vo.CommentInfo;
import com.gasen.usercenterbackend.service.ICommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements ICommentService {
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private PostMapper postMapper;

    @Override
    public Long addComment(AddComment addComment) {
        try {
            Comment comment = addComment.toComment();
            int insert = commentMapper.insert(comment);
            if (insert > 0) {
                return comment.getId();
            }
            return -1L;
        } catch (Exception e) {
            log.error("添加评论异常", e);
            return -1L;
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
            log.error("减少子评论数量异常，评论不存在");
            return false;
        }
        comment.setComments(comment.getComments() - 1);
        return commentMapper.updateById(comment) > 0;
    }

    @Override
    public List<CommentInfo> getCommentsByPostUserId(Integer userId, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户帖子评论列表参数异常，用户ID不合法");
                return null;
            }

            // 首先找出该用户发布的所有帖子ID
            LambdaQueryWrapper<Post> postQueryWrapper = new LambdaQueryWrapper<>();
            postQueryWrapper.eq(Post::getAppId, userId);
            postQueryWrapper.eq(Post::getIsDelete, 0); // 只查询未删除的帖子
            List<Post> userPosts = postMapper.selectList(postQueryWrapper);

            if (userPosts == null || userPosts.isEmpty()) {
                return List.of(); // 用户没有发帖，返回空列表
            }

            // 获取用户所有帖子的ID
            List<Long> postIds = userPosts.stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());

            // 查询这些帖子下的评论
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.in(Comment::getPostId, postIds);
            commentQueryWrapper.eq(Comment::getIsDelete, 0); // 只查询未删除的评论
            commentQueryWrapper.orderByDesc(Comment::getCreateTime); // 按创建时间降序排序

            // 分页查询
            Page<Comment> page = new Page<>(pageNum, pageSize);
            Page<Comment> commentPage = commentMapper.selectPage(page, commentQueryWrapper);

            // 将查询结果转换为CommentInfo列表
            if (commentPage.getRecords().isEmpty()) {
                return List.of(); // 返回空列表
            }

            // 创建帖子ID到帖子的映射，用于快速查找帖子信息
            final var postMap = userPosts.stream()
                    .collect(Collectors.toMap(Post::getId, post -> post));

            return commentPage.getRecords().stream()
                    .map(comment -> {
                        CommentInfo commentInfo = comment.toCommentInfo();
                        // 设置帖子标题
                        Post post = postMap.get(comment.getPostId());
                        if (post != null) {
                            commentInfo.setPostTitle(post.getTitle());
                        }
                        return commentInfo;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户帖子评论列表异常", e);
            return null;
        }
    }
}
