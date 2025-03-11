package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.Request.AddComment;
import com.gasen.usercenterbackend.model.respond.CommentInfo;
import com.gasen.usercenterbackend.model.Request.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.IPostService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class CommentController {

    @Resource
    private ICommentService commentService;

    @Resource
    private IPostService postService;

    // 1. 新增评论
    @PostMapping("/addComment")
    public BaseResponse addComment(@RequestBody AddComment addComment) {
        try {
            if (addComment == null || addComment.getUserId() == null || addComment.getPostId() == null || addComment.getContent() == null){
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = commentService.addComment(addComment);
            if (result) {
                return ResultUtils.success("添加评论成功！");
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "添加评论失败！");
            }
        } catch (Exception e) {
            log.error("添加评论异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "添加评论失败！");
        }
    }

    // 2. 删除评论，根据 commentId 与 userId 校验后删除
    @PostMapping("/deleteComment")
    @Transactional
    public BaseResponse deleteComment(@RequestParam Long postId, @RequestParam Long commentId, @RequestParam Integer userId) {
        try {
            if (commentId == null || userId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = commentService.deleteComment(commentId, userId) && postService.reduceComments(postId);
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
    @PostMapping("/updateComment")
    public BaseResponse updateComment(@RequestBody UpdateComment updateComment) {
        try {
            if (updateComment == null || updateComment.getCommentId() == null || updateComment.getUserId() == null || updateComment.getContent() == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = commentService.updateComment(updateComment);
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

    // 4. 查询评论列表（根据帖子 ID 查询该帖子的所有评论）
    @GetMapping("/getCommentList")
    public BaseResponse getCommentList(@RequestParam Long postId) {
        try {
            if (postId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            List<Comment> commentList = commentService.getCommentListByPost(postId);
            // commentList 变 commentInfoList
            List<CommentInfo> commentInfoList = commentList.stream().map(Comment::toCommentInfo).toList();

            return ResultUtils.success(commentInfoList);
        } catch (Exception e) {
            log.error("查询评论列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询评论列表失败！");
        }
    }

    // 5. 查询单个评论详情
    @PostMapping("/getCommentDetail")
    public BaseResponse getCommentDetail(@RequestParam Long commentId) {
        try {
            if (commentId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            Comment comment = commentService.getComment(commentId);
            return ResultUtils.success(comment.toCommentDetail());
        } catch (Exception e) {
            log.error("查询评论详情异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询评论详情失败！");
        }
    }
}
