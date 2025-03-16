package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.dto.AddSubComment;
import com.gasen.usercenterbackend.model.dto.UpdateSubComment;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;

import java.util.List;

public interface ISubCommentService extends ILikesService {
    List<SubComment> getSubCommentList(Long commentId);

    List<SubCommentDetail> getSubCommentsByCommentId(Long commentId);

    Long addSubComment(AddSubComment addSubComment);

    boolean deleteSubComment(Long subCommentId, Integer userId);

    boolean updateSubComment(UpdateSubComment updateSubComment);
}
