package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.dto.AddComment;
import com.gasen.usercenterbackend.model.dto.AddSubComment;
import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dto.UpdateSubComment;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.ILikesService;
import com.gasen.usercenterbackend.service.ISubCommentService;
import com.gasen.usercenterbackend.utils.LikesUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.gasen.usercenterbackend.constant.LikeConstant.COMMENT_TYPE;
import static com.gasen.usercenterbackend.constant.LikeConstant.SUB_COMMENT_TYPE;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class SubCommentController {

    @Resource
    private ISubCommentService subCommentService;

    @Resource
    private ICommentService commentService;

    @Resource
    private LikesUtil likesUtil;

    // 1. 新增评论
    @PostMapping("/addSubComment")
    @Transactional
    public BaseResponse addComment(@RequestBody AddSubComment addSubComment) {
        try {
            if (addSubComment == null || addSubComment.getUserId() == null || addSubComment.getCommentId() == null
                    || addSubComment.getContent() == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            Long id = subCommentService.addSubComment(addSubComment);
            boolean result = id > 0 && commentService.addComments(
                    addSubComment.getCommentId());
            if (result) {
                return ResultUtils.success(id);
            } else {
                log.error("添加评论异常");
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "添加评论失败！");
            }
        } catch (Exception e) {
            log.error("添加评论异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "添加评论失败！");
        }
    }

    // 2. 删除评论，根据 subCommentId 与 userId 校验后删除
    @PostMapping("/deleteSubComment")
    @Transactional
    public BaseResponse deleteSubComment(@RequestParam Long commentId, @RequestParam Long subCommentId,
            @RequestParam Long userId) {
        try {
            if (subCommentId == null || userId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = subCommentService.deleteSubComment(subCommentId, userId)
                    && commentService.reduceComments(commentId);
            if (result) {
                return ResultUtils.success("删除评论成功！");
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除评论失败！");
            }
        } catch (Exception e) {
            log.error("删除评论异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除评论失败！");
        }
    }

    // 3. 修改评论，传入 UpdateComment 对象，根据 commentId 查找并比对 userId 后修改内容
    @PostMapping("/updateSubComment")
    public BaseResponse updateSubComment(@RequestBody UpdateSubComment updateSubComment) {
        try {
            if (updateSubComment == null || updateSubComment.getSubCommentId() == null
                    || updateSubComment.getUserId() == null
                    || updateSubComment.getContent() == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = subCommentService.updateSubComment(updateSubComment);
            if (result) {
                return ResultUtils.success("修改评论成功！");
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "修改评论失败！");
            }
        } catch (Exception e) {
            log.error("修改评论异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "修改评论失败！");
        }
    }

    // 4. 根据评论id获取所有子评论
    @Operation(summary = "根据评论id获取所有子评论")
    @PostMapping("/getSubCommentList")
    public BaseResponse getSubCommentList(@RequestParam Long commentId) {
        if (commentId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "主评论id为空");
        List<SubComment> subCommentList = subCommentService.getSubCommentList(commentId);
        if (subCommentList == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "子评论为空");
        if (subCommentList.isEmpty())
            return ResultUtils.success("ok");
        // 转为SubCommentDetail
        List<SubCommentDetail> res = subCommentList.stream().map(SubComment::toSubCommentDetail).toList();
        return ResultUtils.success(res);
    }

    @PostMapping("/likeSubComment")
    public BaseResponse likeSubComment(@RequestParam Long subCommentId) {
        try {
            if (subCommentId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = likesUtil.like(SUB_COMMENT_TYPE, subCommentId);
            if (!result)
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞失败！");

            return ResultUtils.success("点赞成功！");
        } catch (Exception e) {
            log.error("点赞评论异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞失败！");
        }
    }

}
