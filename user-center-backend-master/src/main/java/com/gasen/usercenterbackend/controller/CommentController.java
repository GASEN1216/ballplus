package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dto.AddComment;
import com.gasen.usercenterbackend.model.vo.CommentInfo;
import com.gasen.usercenterbackend.model.dto.UpdateComment;
import com.gasen.usercenterbackend.model.dao.Comment;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.model.vo.SubCommentInfo;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.ISubCommentService;
import com.gasen.usercenterbackend.utils.LikesUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gasen.usercenterbackend.constant.LikeConstant.COMMENT_TYPE;
import static com.gasen.usercenterbackend.constant.LikeConstant.POST_TYPE;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class CommentController {

    @Resource
    private ICommentService commentService;

    @Resource
    private IPostService postService;

    @Resource
    private ISubCommentService subCommentService;

    @Resource
    private LikesUtil likesUtil;

    // 1. 新增评论
    @PostMapping("/addComment")
    @Transactional
    public BaseResponse addComment(@RequestBody AddComment addComment) {
        try {
            if (addComment == null || addComment.getUserId() == null || addComment.getPostId() == null
                    || addComment.getContent() == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }

            Long commentId = commentService.addComment(addComment);
            boolean result = commentId > 0 && postService.addComments(addComment.getPostId());

            if (result) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("commentId", commentId);
                resultMap.put("message", "评论成功！");
                return ResultUtils.success(resultMap);
            } else {
                log.error("添加评论异常");
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
    public BaseResponse deleteComment(@RequestParam Long postId, @RequestParam Long commentId,
            @RequestParam Integer userId) {
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
            if (updateComment == null || updateComment.getCommentId() == null || updateComment.getUserId() == null
                    || updateComment.getContent() == null) {
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

    // 6.点赞评论
    @PostMapping("/likeComment")
    public BaseResponse likeComment(@RequestParam Long commentId) {
        try {
            if (commentId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = likesUtil.like(COMMENT_TYPE, commentId);
            if (!result)
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞失败！");

            return ResultUtils.success("点赞成功！");
        } catch (Exception e) {
            log.error("点赞评论异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞失败！");
        }
    }

    /**
     * 获取用户发布的帖子收到的评论列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 评论列表及分页信息
     */
    @PostMapping("/getMyPostComments")
    public BaseResponse getMyPostComments(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (userId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空！");
            }

            // 获取用户发帖的评论
            List<CommentInfo> comments = commentService.getCommentsByPostUserId(userId, pageNum, pageSize);

            if (comments == null) {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取评论失败！");
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("comments", comments);

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取用户帖子评论列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取用户帖子评论列表失败！");
        }
    }

    /**
     * 获取用户评论收到的回复列表
     * 
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 回复列表及分页信息
     */
    @PostMapping("/getMyCommentReplies")
    public BaseResponse getMyCommentReplies(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (userId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空！");
            }

            // 获取用户评论收到的回复
            List<SubCommentInfo> replies = subCommentService.getRepliesByCommentUserId(userId, pageNum, pageSize);

            if (replies == null) {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取回复失败！");
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("replies", replies);

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取用户评论回复列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取用户评论回复列表失败！");
        }
    }
}
