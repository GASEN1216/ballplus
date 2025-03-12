package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.mapper.SubCommentMapper;
import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.respond.SubCommentDetail;
import com.gasen.usercenterbackend.service.IPostService;
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
}
