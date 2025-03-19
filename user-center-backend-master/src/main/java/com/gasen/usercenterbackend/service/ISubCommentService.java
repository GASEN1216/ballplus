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

    /**
     * 获取用户评论收到的回复列表
     * 
     * @param userId   评论作者的用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 回复详情列表
     */
    List<SubCommentDetail> getRepliesByCommentUserId(Integer userId, Integer pageNum, Integer pageSize);
}
