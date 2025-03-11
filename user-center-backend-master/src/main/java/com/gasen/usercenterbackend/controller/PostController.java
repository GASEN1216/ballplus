package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.Request.AddPost;
import com.gasen.usercenterbackend.model.respond.PostDetail;
import com.gasen.usercenterbackend.model.respond.PostInfo;
import com.gasen.usercenterbackend.model.Request.UpdatePost;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class PostController {

    @Resource
    private IPostService postService;

    @Resource
    private IUserService userService;

    // 1. 新增帖子，接收 AddPost 对象
    @PostMapping("/addPost")
    public BaseResponse addPost(@RequestBody AddPost addPost) {
        try {
            if(addPost == null || addPost.getAddId() == null || addPost.getTitle() == null || addPost.getContent() == null)
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            boolean result = postService.addPost(addPost.toPost());
            if (result) {
                return ResultUtils.success("参加活动成功！");
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "参加活动失败！");
            }
        } catch (Exception e) {
            log.error("新增帖子异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "参加活动失败！");
        }
    }

    // 2. 删除帖子，根据 postId 与 userId 比较 appId 是否匹配
    @PostMapping("/deletePost")
    @Transactional
    public BaseResponse deletePost(@RequestParam Long postId, @RequestParam Integer userId) {
        try {
            boolean result = postService.deletePost(postId, userId);
            if (result) {
                return ResultUtils.success("删除成功！");
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除失败！");
            }
        } catch (Exception e) {
            log.error("删除帖子异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "删除失败！");
        }
    }

    // 3. 修改帖子，接收 UpdatePost 对象，根据 postId 查找并比对 userId 后修改内容
    @PostMapping("/updatePost")
    public BaseResponse updatePost(@RequestBody UpdatePost updatePost) {
        try {
            if (updatePost == null || updatePost.getPostId() == null || updatePost.getUserId() == null || updatePost.getContent() == null)
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            boolean result = postService.updatePost(updatePost.toPost());
            if (result) {
                return ResultUtils.success("修改成功！");
            } else {
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "修改失败！");
            }
        } catch (Exception e) {
            log.error("修改帖子异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "修改失败！");
        }
    }

    // 4. 查询所有帖子的粗略信息，返回 List<PostInfo>
    @GetMapping("/getPostList")
    public BaseResponse getPostList() {
        try {
            List<Post> postList = postService.getPostList();
            if (postList == null)
                return ResultUtils.error(ErrorCode.POST_NOT_FOUND, "查询帖子失败！");
            List<PostInfo> postInfoList = postList.stream()
                    .map(Post::toPostInfo)
                    .toList();
            return ResultUtils.success(postInfoList);
        } catch (Exception e) {
            log.error("查询帖子列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询帖子列表失败！");
        }
    }

    // 5. 查询单个帖子的详细信息，返回 PostDetail 对象
    @PostMapping("/getPostDetail")
    public BaseResponse getPostDetail(@RequestParam Long postId) {
        try {
            if (postId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            PostDetail postDetail = postService.getPostDetail(postId);
            if (postDetail == null) {
                return ResultUtils.error(ErrorCode.POST_NOT_FOUND, "帖子不存在！");
            }
            return ResultUtils.success(postDetail);
        } catch (Exception e) {
            log.error("查询帖子详情异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询帖子详情失败！");
        }
    }
}
