package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.dto.AddSubComment;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.UpdateSubComment;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.model.vo.SubCommentInfo;

import java.util.List;

public interface ISubCommentService extends ILikesService {
    List<SubComment> getSubCommentList(Long commentId);

    List<SubCommentDetail> getSubCommentsByCommentId(Long commentId);

    Integer getLikesById(Long id);

    boolean updateLikes(Long id, Integer likes);

    Long addSubComment(AddSubComment addSubComment);

    boolean deleteSubComment(Long subCommentId, Long userId);

    boolean updateSubComment(UpdateSubComment updateSubComment);

    /**
     * 获取用户评论收到的回复列表（传统分页）
     * 
     * @param userId   评论作者的用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 回复详情列表
     * @deprecated 使用 getRepliesByCommentUserIdWithCursor 替代
     */
    @Deprecated
    List<SubCommentInfo> getRepliesByCommentUserId(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 使用游标分页获取用户评论收到的回复列表
     * 
     * @param userId 评论作者的用户ID
     * @param cursorRequest 游标分页请求
     * @return 游标分页响应
     */
    CursorPageResponse<SubCommentInfo> getRepliesByCommentUserIdWithCursor(Long userId, CursorPageRequest cursorRequest);
}
