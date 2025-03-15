package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.SubCommentMapper;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SubCommentServiceImpl implements ISubCommentService {
    @Resource
    private SubCommentMapper subCommentMapper;

    @Override
    public List<SubComment> getSubCommentList(Long commentId) {
        return subCommentMapper.selectList(new LambdaQueryWrapper<SubComment>().eq(SubComment::getCommentId, commentId));
    }

    @Override
    public List<SubCommentDetail> getSubCommentsByCommentId(Long commentId) {
        // 找出所有comment_id == commentId的子评论
        List<SubComment> subComments = getSubCommentList(commentId);
        if (subComments == null){
            log.info("查找子评论失败！");
            return null;
        }
        if (subComments.isEmpty()){
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
}
