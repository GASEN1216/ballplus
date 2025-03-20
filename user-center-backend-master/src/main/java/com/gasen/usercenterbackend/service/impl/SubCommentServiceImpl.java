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
import com.gasen.usercenterbackend.model.dto.UpdateSubComment;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.model.vo.SubCommentInfo;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        return subCommentMapper
                .selectList(new LambdaQueryWrapper<SubComment>().eq(SubComment::getCommentId, commentId));
    }

    @Override
    public List<SubCommentDetail> getSubCommentsByCommentId(Long commentId) {
        // 找出所有comment_id == commentId的子评论
        List<SubComment> subComments = getSubCommentList(commentId);
        if (subComments == null) {
            log.info("查找子评论失败！");
            return null;
        }
        if (subComments.isEmpty()) {
            return null;
        }
        return subComments.stream().map(SubComment::toSubCommentDetail).toList();
    }

    @Override
    public Integer getLikesById(Long id) {
        SubComment subComment = subCommentMapper.selectById(id);
        if (subComment == null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "子评论不存在");
        return subComment.getLikes();
    }

    @Override
    public boolean updateLikes(Long id, Integer likes) {
        SubComment subComment = subCommentMapper.selectById(id);
        if (subComment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "帖子不存在");
        }
        if (likes == null || likes < 0)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞数错误");
        subComment.setLikes(likes);
        return subCommentMapper.updateById(subComment) > 0;
    }

    @Override
    public Long addSubComment(AddSubComment addSubComment) {
        SubComment subComment = addSubComment.toSubComment();
        return (long) subCommentMapper.insert(subComment);
    }

    @Override
    public boolean deleteSubComment(Long subCommentId, Integer userId) {
        SubComment subComment = subCommentMapper.selectById(subCommentId);
        if (subComment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "子评论不存在");
        }
        if (subComment.getAppId() != userId) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "子评论不属于当前用户");
        }
        return subCommentMapper.deleteById(subCommentId) > 0;
    }

    // 更新子评论,先根据subCommentId查找，再比对userId后修改内容
    @Override
    public boolean updateSubComment(UpdateSubComment updateSubComment) {
        SubComment subComment = subCommentMapper.selectById(updateSubComment.getSubCommentId());
        if (subComment == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "子评论不存在");
        }
        if (!subComment.getAppId().equals(updateSubComment.getUserId())) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "子评论不属于当前用户");
        }
        subComment.setContent(updateSubComment.getContent());
        return subCommentMapper.updateById(subComment) > 0;
    }

    @Override
    public List<SubCommentInfo> getRepliesByCommentUserId(Integer userId, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null || userId <= 0) {
                log.error("获取用户评论回复列表参数异常，用户ID不合法");
                return null;
            }

            // 首先找出该用户发布的所有评论ID
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.eq(Comment::getAppId, userId);
            commentQueryWrapper.eq(Comment::getIsDelete, 0); // 只查询未删除的评论
            List<Comment> userComments = commentMapper.selectList(commentQueryWrapper);

            if (userComments == null || userComments.isEmpty()) {
                return List.of(); // 用户没有发表评论，返回空列表
            }

            // 获取用户所有评论的ID
            List<Long> commentIds = userComments.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());

            // 查询这些评论下的回复
            LambdaQueryWrapper<SubComment> subCommentQueryWrapper = new LambdaQueryWrapper<>();
            subCommentQueryWrapper.in(SubComment::getCommentId, commentIds);
            subCommentQueryWrapper.eq(SubComment::getIsDelete, 0); // 只查询未删除的回复
            subCommentQueryWrapper.orderByDesc(SubComment::getCreateTime); // 按创建时间降序排序

            // 分页查询
            Page<SubComment> page = new Page<>(pageNum, pageSize);
            Page<SubComment> subCommentPage = subCommentMapper.selectPage(page, subCommentQueryWrapper);

            // 将查询结果转换为SubCommentInfo列表
            if (subCommentPage.getRecords().isEmpty()) {
                return List.of(); // 返回空列表
            }

            return subCommentPage.getRecords().stream()
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
                        
                        // 获取父评论内容和发布者ID
                        Comment parentComment = commentMapper.selectById(subComment.getCommentId());
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
}
