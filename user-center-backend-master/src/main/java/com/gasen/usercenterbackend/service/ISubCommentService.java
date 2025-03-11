package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.SubComment;

import java.util.List;

public interface ISubCommentService {
    List<SubComment> getSubCommentList(Long commentId);
}
