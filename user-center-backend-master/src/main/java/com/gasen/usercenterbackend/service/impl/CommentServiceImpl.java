package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.CommentMapper;
import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dto.AddComment;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.model.vo.CommentInfo;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private IUserService userService;

    @Resource
    private PostMapper postMapper;

    @Lazy
    @Resource
    private IPostService postService;

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
    public boolean deleteComment(Long commentId, Long userId) {
        Comment comment = new Comment();
        comment.setIsDelete((byte) 1);
        return commentMapper.update(comment,
            new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .eq(Comment::getAppId, userId)
                .eq(Comment::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public boolean updateComment(UpdateComment updateComment) {
        try {
            Comment comment = new Comment();
            comment.setContent(updateComment.getContent());
            return commentMapper.update(comment,
                new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getId, updateComment.getCommentId())
                    .eq(Comment::getAppId, updateComment.getUserId())
                    .eq(Comment::getIsDelete, 0)
            ) > 0;
        } catch (Exception e) {
            log.error("修改评论异常", e);
            return false;
        }
    }

    @Override
    public List<Comment> getCommentListByPost(Long postId) {
        try {
            return commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                    .select(Comment::getId, Comment::getAppId, Comment::getAppName,
                           Comment::getAvatar, Comment::getContent, Comment::getLikes,
                           Comment::getComments, Comment::getCreateTime)
                    .eq(Comment::getPostId, postId)
                    .eq(Comment::getIsDelete, 0)
                    .orderByDesc(Comment::getCreateTime)
            );
        } catch (Exception e) {
            log.error("查询评论列表异常", e);
            return null;
        }
    }

    @Override
    public Comment getComment(Long commentId) {
        return commentMapper.selectOne(
            new LambdaQueryWrapper<Comment>()
                .select(Comment::getId, Comment::getAppId, Comment::getAppName,
                       Comment::getAvatar, Comment::getContent, Comment::getLikes,
                       Comment::getComments, Comment::getCreateTime)
                .eq(Comment::getId, commentId)
                .eq(Comment::getIsDelete, 0)
        );
    }

    @Override
    public List<CommentDetail> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>()
                .select(Comment::getId, Comment::getAppId, Comment::getAppName,
                       Comment::getAvatar, Comment::getContent, Comment::getLikes,
                       Comment::getComments, Comment::getCreateTime)
                .eq(Comment::getPostId, postId)
                .eq(Comment::getIsDelete, 0)
                .orderByDesc(Comment::getCreateTime)
        );
        
        if (comments == null) {
            return new ArrayList<>();
        }
        return comments.stream().map(Comment::toCommentDetail).toList();
    }

    @Override
    public Integer getLikesById(Long id) {
        Comment comment = commentMapper.selectOne(
            new LambdaQueryWrapper<Comment>()
                .select(Comment::getLikes)
                .eq(Comment::getId, id)
                .eq(Comment::getIsDelete, 0)
        );
        
        if (comment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "评论不存在");
        }
        return comment.getLikes();
    }

    @Override
    public boolean updateLikes(Long id, Integer likes) {
        if (likes == null || likes < 0) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞数错误");
        }
        
        Comment comment = new Comment();
        comment.setLikes(likes);
        return commentMapper.update(comment,
            new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, id)
                .eq(Comment::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public boolean addComments(Long commentId) {
        return commentMapper.update(null,
            new UpdateWrapper<Comment>()
                .eq("id", commentId)
                .eq("is_delete", 0)
                .setSql("comments = comments + 1")
        ) > 0;
    }

    @Override
    public boolean reduceComments(Long commentId) {
        return commentMapper.update(null,
            new UpdateWrapper<Comment>()
                .eq("id", commentId)
                .eq("is_delete", 0)
                .setSql("comments = comments - 1")
        ) > 0;
    }

    @Override
    @Deprecated
    public List<CommentInfo> getCommentsByPostUserId(Long userId, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户帖子评论列表参数异常，用户ID不合法");
                return null;
            }

            // 首先找出该用户发布的所有帖子ID
            LambdaQueryWrapper<Post> postQueryWrapper = new LambdaQueryWrapper<>();
            postQueryWrapper.select(Post::getId, Post::getTitle)
                          .eq(Post::getAppId, userId)
                          .eq(Post::getIsDelete, 0);
            List<Post> userPosts = postMapper.selectList(postQueryWrapper);

            if (userPosts == null || userPosts.isEmpty()) {
                return List.of();
            }

            // 获取用户所有帖子的ID
            List<Long> postIds = userPosts.stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());

            // 查询这些帖子下的评论
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.select(Comment::getId, Comment::getAppId, Comment::getAppName,
                                    Comment::getAvatar, Comment::getContent, Comment::getLikes,
                                    Comment::getComments, Comment::getCreateTime, Comment::getPostId)
                             .in(Comment::getPostId, postIds)
                             .eq(Comment::getIsDelete, 0)
                             .orderByDesc(Comment::getCreateTime);

            // 使用游标分页
            commentQueryWrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
            
            List<Comment> records = commentMapper.selectList(commentQueryWrapper);
            
            if (records.isEmpty()) {
                return List.of();
            }

            // 创建帖子ID到帖子的映射，用于快速查找帖子信息
            final var postMap = userPosts.stream()
                    .collect(Collectors.toMap(Post::getId, post -> post));

            return records.stream()
                    .map(comment -> {
                        CommentInfo commentInfo = comment.toCommentInfo();
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
    
    @Override
    public CursorPageResponse<CommentInfo> getCommentsByPostUserIdWithCursor(Long userId, CursorPageRequest cursorRequest) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户帖子评论列表参数异常，用户ID不合法");
                throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户ID不合法");
            }

            // 首先找出该用户发布的所有帖子ID
            LambdaQueryWrapper<Post> postQueryWrapper = new LambdaQueryWrapper<>();
            postQueryWrapper.select(Post::getId, Post::getTitle)
                          .eq(Post::getAppId, userId)
                          .eq(Post::getIsDelete, 0);
            List<Post> userPosts = postMapper.selectList(postQueryWrapper);

            if (userPosts == null || userPosts.isEmpty()) {
                return CursorPageResponse.build(List.of(), null, false);
            }

            // 获取用户所有帖子的ID
            List<Long> postIds = userPosts.stream()
                    .map(Post::getId)
                    .collect(Collectors.toList());

            // 查询这些帖子下的评论
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.select(Comment::getId, Comment::getAppId, Comment::getAppName,
                                     Comment::getAvatar, Comment::getContent, Comment::getLikes,
                                     Comment::getComments, Comment::getCreateTime, Comment::getPostId, Comment::getGrade)
                             .in(Comment::getPostId, postIds)
                             .eq(Comment::getIsDelete, 0);
            
            // 使用创建时间作为游标
            if (StringUtils.isNotBlank(cursorRequest.getCursor())) {
                // 如果是升序查询
                if (Boolean.TRUE.equals(cursorRequest.getAsc())) {
                    commentQueryWrapper.gt(Comment::getCreateTime, cursorRequest.getCursor());
                    commentQueryWrapper.orderByAsc(Comment::getCreateTime);
                } else {
                    // 降序查询
                    commentQueryWrapper.lt(Comment::getCreateTime, cursorRequest.getCursor());
                    commentQueryWrapper.orderByDesc(Comment::getCreateTime);
                }
            } else {
                // 首次查询，没有游标，按创建时间排序
                if (Boolean.TRUE.equals(cursorRequest.getAsc())) {
                    commentQueryWrapper.orderByAsc(Comment::getCreateTime);
                } else {
                    commentQueryWrapper.orderByDesc(Comment::getCreateTime);
                }
            }

            // 多取一条数据，用于判断是否还有更多数据
            Integer pageSize = cursorRequest.getPageSize();
            commentQueryWrapper.last("LIMIT " + (pageSize + 1));
            
            List<Comment> records = commentMapper.selectList(commentQueryWrapper);
            
            boolean hasMore = false;
            String nextCursor = null;
            
            // 如果查询结果数量超过pageSize，说明还有更多数据
            if (records.size() > pageSize) {
                hasMore = true;
                // 移除多查询的一条数据
                Comment lastComment = records.remove(records.size() - 1);
                nextCursor = lastComment.getCreateTime().toString();
            } else if (!records.isEmpty()) {
                // 如果结果数量等于pageSize，使用最后一条数据的创建时间作为下一个游标
                nextCursor = records.get(records.size() - 1).getCreateTime().toString();
            }
            
            if (records.isEmpty()) {
                return CursorPageResponse.build(List.of(), nextCursor, hasMore);
            }

            // 创建帖子ID到帖子的映射，用于快速查找帖子信息
            final var postMap = userPosts.stream()
                    .collect(Collectors.toMap(Post::getId, post -> post));

            // 将实体转换为VO
            List<CommentInfo> commentInfoList = records.stream()
                    .map(comment -> {
                        CommentInfo commentInfo = comment.toCommentInfo();
                        Post post = postMap.get(comment.getPostId());
                        if (post != null) {
                            commentInfo.setPostTitle(post.getTitle());
                        }
                        return commentInfo;
                    })
                    .collect(Collectors.toList());
            
            return CursorPageResponse.build(commentInfoList, nextCursor, hasMore);
        } catch (BusinessExcetion e) {
            throw e;
        } catch (Exception e) {
            log.error("游标分页获取用户帖子评论列表异常", e);
            throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR, "查询用户帖子评论列表失败");
        }
    }
        @Override
    public Page<CommentInfo> getAllCommentsAdmin(long pageNum, long pageSize) {
        // 1. 创建分页对象
        Page<Comment> commentPage = new Page<>(pageNum, pageSize);

        // 2. 执行分页查询 (ServiceImpl 提供了 page 方法)
        //    可以根据需要添加排序等
        page(commentPage); // 现在应该能找到 page 方法

        List<Comment> records = commentPage.getRecords();
        if (records.isEmpty()) {
            return new Page<>(commentPage.getCurrent(), commentPage.getSize(), 0);
        }

        // 3. 获取评论关联的用户ID列表 (使用 appId)
        Set<Long> userIds = records.stream().map(Comment::getAppId).collect(Collectors.toSet()); // 使用 getAppId

        // 4. 查询用户信息
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 5. 组装 CommentInfo 列表
        List<CommentInfo> commentInfoList = records.stream().map(comment -> {
            // 调用 toCommentInfo() 方法进行转换
            CommentInfo commentInfo = comment.toCommentInfo();

            // 获取关联的 User 对象 (可能 toCommentInfo 内部会用到，或者未来扩展用)
            User user = userMap.get(comment.getAppId());

            // 不再需要 setUser, 假设 toCommentInfo 已处理好 CommentInfo 中的字段
            // if (user != null) {
            //     commentInfo.setUser(user);
            // }

            // 按需填充其他 toCommentInfo 未处理的字段 (如果需要)
            // 例如，如果 CommentInfo 需要 postTitle 而 Comment 没有此字段:
            // Post post = postMap.get(comment.getPostId()); // 需要先查询 postMap
            // if (post != null) { commentInfo.setPostTitle(post.getTitle()); }

            return commentInfo;
        }).collect(Collectors.toList());

        // 6. 创建并返回 CommentInfo 的分页结果
        Page<CommentInfo> finalPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        finalPage.setRecords(commentInfoList);
        return finalPage;
    }

    @Override
    @Transactional // 添加事务注解
    public boolean deleteCommentAdminById(Long commentId) {
        if (commentId == null || commentId <= 0) {
            return false;
        }
        // 1. 查找评论 (ServiceImpl 提供了 getById 方法)
        Comment comment = this.getById(commentId); // 现在应该能找到 getById
        if (comment == null) {
            return false;
        }

        // 2. 执行删除操作 (ServiceImpl 提供了 removeById 方法)
        boolean deleted = this.removeById(commentId); // 现在应该能找到 removeById

        // 3. 如果删除成功，更新帖子评论数
        if (deleted && comment.getPostId() != null) {
            boolean countUpdated = postService.reduceComments(comment.getPostId()); // 现在应该能找到 postService
            if (!countUpdated) {
                log.warn("Failed to reduce comment count for post {} after deleting comment {}", comment.getPostId(), commentId);
            }
        }

        return deleted;
    }
}
