package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.mapper.SubCommentMapper;
import com.gasen.usercenterbackend.model.dao.SubComment;
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
}
