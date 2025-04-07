package com.gasen.usercenterbackend.service.impl;

import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.model.vo.PostDetail;
import com.gasen.usercenterbackend.model.vo.PostInfo;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class PostServiceImpl implements IPostService {
    @Resource
    private PostMapper postMapper;

    @Resource
    private ICommentService commentService;

    @Resource
    private ISubCommentService subCommentService;

    @Override
    public boolean addPost(Post post) {
        return postMapper.insert(post) > 0;
    }

    @Override
    public PostDetail getPostDetail(Long postId) {
        Post post = postMapper.selectOne(
            new LambdaQueryWrapper<Post>()
                .select(Post::getId, Post::getAppId, Post::getAppName, Post::getAvatar,
                       Post::getGrade, Post::getTitle, Post::getContent, Post::getLikes,
                       Post::getComments, Post::getPicture, Post::getCreateTime,
                       Post::getUpdateTime, Post::getUpdateContentTime)
                .eq(Post::getId, postId)
                .eq(Post::getIsDelete, 0)
        );
        
        if (post == null) {
            log.error("查询帖子详情异常，帖子不存在");
            return null;
        }
        PostDetail postDetail = post.toPostDetail();
        // 查询主评论
        List<CommentDetail> commentList = commentService.getCommentsByPostId(postId);

        for (CommentDetail comment : commentList) {
            // 查询该评论的子评论
            List<SubCommentDetail> subComments = subCommentService.getSubCommentsByCommentId(comment.getCommentId());
            comment.setSubComments(subComments);
        }
        postDetail.setCommentsList(commentList);

        return postDetail;
    }

    @Override
    public boolean deletePost(Long postId, Long userId) {
        Post post = new Post();
        post.setIsDelete((byte) 1);
        return postMapper.update(post,
            new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getAppId, userId)
                .eq(Post::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public boolean updatePost(Post post) {
        post.setUpdateContentTime(LocalDateTime.now());
        return postMapper.update(post,
            new LambdaQueryWrapper<Post>()
                .eq(Post::getId, post.getId())
                .eq(Post::getAppId, post.getAppId())
                .eq(Post::getIsDelete, 0)
        ) > 0;
    }

    @Override
    @Deprecated
    public Page<Post> getPostList(long pageNum, long pageSize, String keyword) {
        try {
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Post::getIsDelete, 0);

            if (StringUtils.isNotBlank(keyword)) {
                queryWrapper.and(qw -> qw.like(Post::getTitle, keyword + "%")
                                         .or().like(Post::getContent, keyword + "%")
                                         .or().like(Post::getAppName, keyword + "%"));
            }

            queryWrapper.orderByDesc(Post::getUpdateTime);
            
            queryWrapper.select(Post::getId, Post::getAppId, Post::getAppName, Post::getAvatar,
                              Post::getGrade, Post::getTitle, Post::getContent, Post::getLikes,
                              Post::getComments, Post::getPicture, Post::getCreateTime,
                              Post::getUpdateTime, Post::getUpdateContentTime);

            queryWrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
            
            List<Post> records = postMapper.selectList(queryWrapper);
            long total = postMapper.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getIsDelete, 0));
            
            Page<Post> page = new Page<>(pageNum, pageSize);
            page.setRecords(records);
            page.setTotal(total);

            return page;
        } catch (Exception e) {
            log.error("分页获取帖子列表异常", e);
            throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR, "查询帖子列表失败");
        }
    }
    
    @Override
    public CursorPageResponse<Post> getPostListWithCursor(CursorPageRequest cursorRequest, String keyword) {
        try {
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Post::getIsDelete, 0);
            
            // 添加关键字搜索条件
            if (StringUtils.isNotBlank(keyword)) {
                queryWrapper.and(qw -> qw.like(Post::getTitle, keyword + "%")
                                         .or().like(Post::getContent, keyword + "%")
                                         .or().like(Post::getAppName, keyword + "%"));
            }
            
            // 使用更新时间作为游标
            if (StringUtils.isNotBlank(cursorRequest.getCursor())) {
                // 如果是升序查询
                if (Boolean.TRUE.equals(cursorRequest.getAsc())) {
                    queryWrapper.gt(Post::getUpdateTime, cursorRequest.getCursor());
                    queryWrapper.orderByAsc(Post::getUpdateTime);
                } else {
                    // 降序查询
                    queryWrapper.lt(Post::getUpdateTime, cursorRequest.getCursor());
                    queryWrapper.orderByDesc(Post::getUpdateTime);
                }
            } else {
                // 首次查询，没有游标，按更新时间排序
                if (Boolean.TRUE.equals(cursorRequest.getAsc())) {
                    queryWrapper.orderByAsc(Post::getUpdateTime);
                } else {
                    queryWrapper.orderByDesc(Post::getUpdateTime);
                }
            }
            
            // 查询需要的字段
            queryWrapper.select(Post::getId, Post::getAppId, Post::getAppName, Post::getAvatar,
                              Post::getGrade, Post::getTitle, Post::getContent, Post::getLikes,
                              Post::getComments, Post::getPicture, Post::getCreateTime,
                              Post::getUpdateTime, Post::getUpdateContentTime);
            
            // 多取一条数据，用于判断是否还有更多数据
            Integer pageSize = cursorRequest.getPageSize();
            queryWrapper.last("LIMIT " + (pageSize + 1));
            
            List<Post> records = postMapper.selectList(queryWrapper);
            
            boolean hasMore = false;
            String nextCursor = null;
            
            // 如果查询结果数量超过pageSize，说明还有更多数据
            if (records.size() > pageSize) {
                hasMore = true;
                // 移除多查询的一条数据
                Post lastPost = records.remove(records.size() - 1);
                nextCursor = lastPost.getUpdateTime().toString();
            } else if (!records.isEmpty()) {
                // 如果结果数量等于pageSize，使用最后一条数据的更新时间作为下一个游标
                nextCursor = records.get(records.size() - 1).getUpdateTime().toString();
            }
            
            return CursorPageResponse.build(records, nextCursor, hasMore);
        } catch (Exception e) {
            log.error("游标分页获取帖子列表异常", e);
            throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR, "查询帖子列表失败");
        }
    }

    @Override
    public boolean reduceComments(Long postId) {
        return postMapper.update(null,
            new UpdateWrapper<Post>()
                .eq("id", postId)
                .eq("is_delete", 0)
                .setSql("comments = comments - 1")
        ) > 0;
    }

    @Override
    public boolean addComments(Long postId) {
        return postMapper.update(null,
            new UpdateWrapper<Post>()
                .eq("id", postId)
                .eq("is_delete", 0)
                .setSql("comments = comments + 1")
        ) > 0;
    }

    @Override
    public Integer getLikesById(Long postId) {
        Post post = postMapper.selectOne(
            new LambdaQueryWrapper<Post>()
                .select(Post::getLikes)
                .eq(Post::getId, postId)
                .eq(Post::getIsDelete, 0)
        );
        
        if (post == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "帖子不存在");
        }
        return post.getLikes();
    }

    @Override
    public boolean updateLikes(Long postId, Integer likes) {
        if (likes == null || likes < 0) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞数错误");
        }
        
        Post post = new Post();
        post.setLikes(likes);
        return postMapper.update(post,
            new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public Post getTopPost() {
        try {
            return postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                    .select(Post::getId, Post::getAppId, Post::getAppName, Post::getAvatar,
                           Post::getGrade, Post::getTitle, Post::getContent, Post::getLikes,
                           Post::getComments, Post::getPicture, Post::getCreateTime,
                           Post::getUpdateTime, Post::getUpdateContentTime)
                    .eq(Post::getIsDelete, 0)
                    .orderByDesc(Post::getLikes)
                    .last("LIMIT 1")
            );
        } catch (Exception e) {
            log.error("获取点赞最高帖子异常", e);
            return null;
        }
    }

    @Override
    @Deprecated
    public List<PostInfo> getPostsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户帖子列表参数异常，用户ID不合法");
                return null;
            }

            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Post::getAppId, userId)
                       .eq(Post::getIsDelete, 0)
                       .orderByDesc(Post::getCreateTime);

            queryWrapper.select(Post::getId, Post::getAppId, Post::getAppName, Post::getAvatar,
                              Post::getGrade, Post::getTitle, Post::getContent, Post::getLikes,
                              Post::getComments, Post::getPicture, Post::getCreateTime,
                              Post::getUpdateTime, Post::getUpdateContentTime);

            queryWrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
            
            List<Post> records = postMapper.selectList(queryWrapper);
            
            if (records.isEmpty()) {
                return List.of();
            }

            return records.stream()
                    .map(Post::toPostInfo)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户帖子列表异常", e);
            return null;
        }
    }
    
    @Override
    public CursorPageResponse<PostInfo> getPostsByUserIdWithCursor(Long userId, CursorPageRequest cursorRequest) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户帖子列表参数异常，用户ID不合法");
                throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户ID不合法");
            }

            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Post::getAppId, userId)
                       .eq(Post::getIsDelete, 0);
            
            // 使用创建时间作为游标
            if (StringUtils.isNotBlank(cursorRequest.getCursor())) {
                // 如果是升序查询
                if (Boolean.TRUE.equals(cursorRequest.getAsc())) {
                    queryWrapper.gt(Post::getCreateTime, cursorRequest.getCursor());
                    queryWrapper.orderByAsc(Post::getCreateTime);
                } else {
                    // 降序查询
                    queryWrapper.lt(Post::getCreateTime, cursorRequest.getCursor());
                    queryWrapper.orderByDesc(Post::getCreateTime);
                }
            } else {
                // 首次查询，没有游标，按创建时间排序
                if (Boolean.TRUE.equals(cursorRequest.getAsc())) {
                    queryWrapper.orderByAsc(Post::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Post::getCreateTime);
                }
            }

            queryWrapper.select(Post::getId, Post::getAppId, Post::getAppName, Post::getAvatar,
                              Post::getGrade, Post::getTitle, Post::getContent, Post::getLikes,
                              Post::getComments, Post::getPicture, Post::getCreateTime,
                              Post::getUpdateTime, Post::getUpdateContentTime);

            // 多取一条数据，用于判断是否还有更多数据
            Integer pageSize = cursorRequest.getPageSize();
            queryWrapper.last("LIMIT " + (pageSize + 1));
            
            List<Post> records = postMapper.selectList(queryWrapper);
            
            boolean hasMore = false;
            String nextCursor = null;
            
            // 如果查询结果数量超过pageSize，说明还有更多数据
            if (records.size() > pageSize) {
                hasMore = true;
                // 移除多查询的一条数据
                Post lastPost = records.remove(records.size() - 1);
                nextCursor = lastPost.getCreateTime().toString();
            } else if (!records.isEmpty()) {
                // 如果结果数量等于pageSize，使用最后一条数据的创建时间作为下一个游标
                nextCursor = records.get(records.size() - 1).getCreateTime().toString();
            }
            
            // 将实体转换为VO
            List<PostInfo> postInfoList = records.stream()
                    .map(Post::toPostInfo)
                    .collect(Collectors.toList());
            
            return CursorPageResponse.build(postInfoList, nextCursor, hasMore);
        } catch (BusinessExcetion e) {
            throw e;
        } catch (Exception e) {
            log.error("游标分页获取用户帖子列表异常", e);
            throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR, "查询用户帖子列表失败");
        }
    }
}
