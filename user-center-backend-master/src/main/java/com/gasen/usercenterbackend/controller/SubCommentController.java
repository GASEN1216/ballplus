package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dao.SubComment;
import com.gasen.usercenterbackend.model.respond.SubCommentDetail;
import com.gasen.usercenterbackend.service.ISubCommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class SubCommentController {

    @Resource
    private ISubCommentService subCommentService;

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

}
