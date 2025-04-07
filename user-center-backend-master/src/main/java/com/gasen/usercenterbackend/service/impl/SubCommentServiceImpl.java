package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.CommentMapper;
import com.gasen.usercenterbackend.mapper.SubCommentMapper;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.dto.AddSubComment;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.UpdateSubComment;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.model.vo.SubCommentInfo;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubCommentServiceImpl implements ISubCommentService {
    @Resource
    private SubCommentMapper subCommentMapper;

    @Resource
    private CommentMapper commentMapper;

    @Override
    public List<SubComment> getSubCommentList(Long commentId) {
        return subCommentMapper.selectList(
            new LambdaQueryWrapper<SubComment>()
                .select(SubComment::getId, SubComment::getAppId, SubComment::getAppName,
                       SubComment::getAvatar, SubComment::getContent, SubComment::getLikes,
                       SubComment::getCreateTime)
                .eq(SubComment::getCommentId, commentId)
                .eq(SubComment::getIsDelete, 0)
                .orderByDesc(SubComment::getCreateTime)
        );
    }

    @Override
    public List<SubCommentDetail> getSubCommentsByCommentId(Long commentId) {
        List<SubComment> subComments = subCommentMapper.selectList(
            new LambdaQueryWrapper<SubComment>()
                .select(SubComment::getId, SubComment::getAppId, SubComment::getAppName,
                       SubComment::getAvatar, SubComment::getContent, SubComment::getLikes,
                       SubComment::getCreateTime)
                .eq(SubComment::getCommentId, commentId)
                .eq(SubComment::getIsDelete, 0)
                .orderByDesc(SubComment::getCreateTime)
        );
        
        if (subComments == null || subComments.isEmpty()) {
            return null;
        }
        return subComments.stream().map(SubComment::toSubCommentDetail).toList();
    }

    @Override
    public Integer getLikesById(Long id) {
        SubComment subComment = subCommentMapper.selectOne(
            new LambdaQueryWrapper<SubComment>()
                .select(SubComment::getLikes)
                .eq(SubComment::getId, id)
                .eq(SubComment::getIsDelete, 0)
        );
        
        if (subComment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "子评论不存在");
        }
        return subComment.getLikes();
    }

    @Override
    public boolean updateLikes(Long id, Integer likes) {
        if (likes == null || likes < 0) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞数错误");
        }
        
        SubComment subComment = new SubComment();
        subComment.setLikes(likes);
        return subCommentMapper.update(subComment,
            new LambdaQueryWrapper<SubComment>()
                .eq(SubComment::getId, id)
                .eq(SubComment::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public Long addSubComment(AddSubComment addSubComment) {
        SubComment subComment = addSubComment.toSubComment();
        return (long) subCommentMapper.insert(subComment);
    }

    @Override
    public boolean deleteSubComment(Long subCommentId, Long userId) {
        SubComment subComment = new SubComment();
        subComment.setIsDelete((byte) 1);
        return subCommentMapper.update(subComment,
            new LambdaQueryWrapper<SubComment>()
                .eq(SubComment::getId, subCommentId)
                .eq(SubComment::getAppId, userId)
                .eq(SubComment::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public boolean updateSubComment(UpdateSubComment updateSubComment) {
        SubComment subComment = new SubComment();
        subComment.setContent(updateSubComment.getContent());
        return subCommentMapper.update(subComment,
            new LambdaQueryWrapper<SubComment>()
                .eq(SubComment::getId, updateSubComment.getSubCommentId())
                .eq(SubComment::getAppId, updateSubComment.getUserId())
                .eq(SubComment::getIsDelete, 0)
        ) > 0;
    }

    @Override
    public List<SubCommentInfo> getRepliesByCommentUserId(Long userId, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户评论回复列表参数异常，用户ID不合法");
                return null;
            }

            // 首先找出该用户发布的所有评论ID
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.select(Comment::getId, Comment::getContent, Comment::getAppId)
                             .eq(Comment::getAppId, userId)
                             .eq(Comment::getIsDelete, 0);
            List<Comment> userComments = commentMapper.selectList(commentQueryWrapper);

            if (userComments == null || userComments.isEmpty()) {
                return List.of();
            }

            // 获取用户所有评论的ID
            List<Long> commentIds = userComments.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());

            // 查询这些评论下的回复
            LambdaQueryWrapper<SubComment> subCommentQueryWrapper = new LambdaQueryWrapper<>();
            subCommentQueryWrapper.select(SubComment::getId, SubComment::getCommentId,
                                       SubComment::getAppId, SubComment::getAppName,
                                       SubComment::getAvatar, SubComment::getGrade,
                                       SubComment::getContent, SubComment::getLikes,
                                       SubComment::getCreateTime)
                                .in(SubComment::getCommentId, commentIds)
                                .eq(SubComment::getIsDelete, 0)
                                .orderByDesc(SubComment::getCreateTime);

            // 使用游标分页
            subCommentQueryWrapper.last("LIMIT " + (pageNum - 1) * pageSize + ", " + pageSize);
            
            List<SubComment> records = subCommentMapper.selectList(subCommentQueryWrapper);
            
            if (records.isEmpty()) {
                return List.of();
            }

            // 创建评论ID到评论的映射，用于快速查找评论信息
            final var commentMap = userComments.stream()
                    .collect(Collectors.toMap(Comment::getId, comment -> comment));

            return records.stream()
                    .map(subComment -> {
                        SubCommentInfo info = new SubCommentInfo();
                        info.setSubCommentId(subComment.getId())
                            .setCommentId(subComment.getCommentId())
                            .setAppName(subComment.getAppName())
                            .setAppId(subComment.getAppId())
                            .setAvatar(subComment.getAvatar())
                            .setGrade(subComment.getGrade())
                            .setContent(subComment.getContent())
                            .setLikes(subComment.getLikes())
                            .setCreateTime(subComment.getCreateTime());
                        
                        // 从映射中获取父评论信息
                        Comment parentComment = commentMap.get(subComment.getCommentId());
                        if (parentComment != null) {
                            info.setOriginalCommentContent(parentComment.getContent());
                            info.setParentCommentAppId(parentComment.getAppId());
                        }
                        
                        return info;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取用户评论回复列表异常", e);
            return null;
        }
    }

    @Override
    public CursorPageResponse<SubCommentInfo> getRepliesByCommentUserIdWithCursor(Long userId, CursorPageRequest cursorRequest) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户评论回复列表参数异常，用户ID不合法");
                return new CursorPageResponse<>(new ArrayList<>(), null, false);
            }

            // 页面大小参数验证
            Integer pageSize = cursorRequest.getPageSize();
            if (pageSize == null || pageSize <= 0 || pageSize > 100) {
                pageSize = 10; // 默认每页10条
            }
            
            // 排序方向
            boolean isAsc = Boolean.TRUE.equals(cursorRequest.getAsc());
            String cursor = cursorRequest.getCursor();
            
            // 首先找出该用户发布的所有评论ID
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.select(Comment::getId, Comment::getContent, Comment::getAppId)
                             .eq(Comment::getAppId, userId)
                             .eq(Comment::getIsDelete, 0);
            List<Comment> userComments = commentMapper.selectList(commentQueryWrapper);

            if (userComments == null || userComments.isEmpty()) {
                return new CursorPageResponse<>(new ArrayList<>(), null, false);
            }

            // 获取用户所有评论的ID
            List<Long> commentIds = userComments.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());

            // 查询这些评论下的回复
            LambdaQueryWrapper<SubComment> subCommentQueryWrapper = new LambdaQueryWrapper<>();
            subCommentQueryWrapper.select(SubComment::getId, SubComment::getCommentId,
                                       SubComment::getAppId, SubComment::getAppName,
                                       SubComment::getAvatar, SubComment::getGrade,
                                       SubComment::getContent, SubComment::getLikes,
                                       SubComment::getCreateTime)
                                .in(SubComment::getCommentId, commentIds)
                                .eq(SubComment::getIsDelete, 0);
                                
            // 根据排序方向和游标添加筛选条件
            if (StringUtils.hasText(cursor)) {
                try {
                    LocalDateTime cursorTime = LocalDateTime.parse(cursor, DateTimeFormatter.ISO_DATE_TIME);
                    if (isAsc) {
                        // 升序查询：获取创建时间大于游标的记录
                        subCommentQueryWrapper.gt(SubComment::getCreateTime, cursorTime);
                        subCommentQueryWrapper.orderByAsc(SubComment::getCreateTime);
                    } else {
                        // 降序查询：获取创建时间小于游标的记录
                        subCommentQueryWrapper.lt(SubComment::getCreateTime, cursorTime);
                        subCommentQueryWrapper.orderByDesc(SubComment::getCreateTime);
                    }
                } catch (Exception e) {
                    log.error("解析游标时间格式异常", e);
                    // 游标解析失败时，按默认排序方式处理
                    subCommentQueryWrapper.orderByDesc(SubComment::getCreateTime);
                }
            } else {
                // 没有游标时，按默认排序方式处理
                if (isAsc) {
                    subCommentQueryWrapper.orderByAsc(SubComment::getCreateTime);
                } else {
                    subCommentQueryWrapper.orderByDesc(SubComment::getCreateTime);
                }
            }
            
            // 限制查询结果数量
            subCommentQueryWrapper.last("LIMIT " + (pageSize + 1)); // 多查询一条用于判断是否有更多数据
            
            List<SubComment> records = subCommentMapper.selectList(subCommentQueryWrapper);
            
            boolean hasMore = false;
            String nextCursor = null;
            
            // 判断是否有更多数据
            if (records.size() > pageSize) {
                hasMore = true;
                // 移除多查询的一条数据
                SubComment lastRecord = records.remove(records.size() - 1);
            }
            
            if (records.isEmpty()) {
                return new CursorPageResponse<>(new ArrayList<>(), null, false);
            }

            // 获取下一个游标的值
            if (hasMore) {
                SubComment lastRecord = records.get(records.size() - 1);
                nextCursor = lastRecord.getCreateTime().format(DateTimeFormatter.ISO_DATE_TIME);
            }

            // 创建评论ID到评论的映射，用于快速查找评论信息
            final var commentMap = userComments.stream()
                    .collect(Collectors.toMap(Comment::getId, comment -> comment));

            List<SubCommentInfo> resultList = records.stream()
                    .map(subComment -> {
                        SubCommentInfo info = new SubCommentInfo();
                        info.setSubCommentId(subComment.getId())
                            .setCommentId(subComment.getCommentId())
                            .setAppName(subComment.getAppName())
                            .setAppId(subComment.getAppId())
                            .setAvatar(subComment.getAvatar())
                            .setGrade(subComment.getGrade())
                            .setContent(subComment.getContent())
                            .setLikes(subComment.getLikes())
                            .setCreateTime(subComment.getCreateTime());
                        
                        // 从映射中获取父评论信息
                        Comment parentComment = commentMap.get(subComment.getCommentId());
                        if (parentComment != null) {
                            info.setOriginalCommentContent(parentComment.getContent());
                            info.setParentCommentAppId(parentComment.getAppId());
                        }
                        
                        return info;
                    })
                    .collect(Collectors.toList());

            return new CursorPageResponse<>(resultList, nextCursor, hasMore);

        } catch (Exception e) {
            log.error("获取用户评论回复列表异常", e);
            return new CursorPageResponse<>(new ArrayList<>(), null, false);
        }
    }
}
