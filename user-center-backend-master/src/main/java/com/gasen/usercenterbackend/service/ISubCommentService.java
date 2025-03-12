package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.respond.SubCommentDetail;

import java.util.List;

public interface ISubCommentService {
    List<SubComment> getSubCommentList(Long commentId);

    List<SubCommentDetail> getSubCommentsByCommentId(Long commentId);
}
