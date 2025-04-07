package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dto.AddPost;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.model.vo.PostDetail;
import com.gasen.usercenterbackend.model.vo.PostInfo;
import com.gasen.usercenterbackend.model.dto.UpdatePost;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.utils.LikesUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gasen.usercenterbackend.constant.LikeConstant.POST_TYPE;
import static com.gasen.usercenterbackend.constant.LikeConstant.REDIS_POST_LIKES;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class PostController {

    @Resource
    private IPostService postService;

    @Resource
    private LikesUtil likesUtil;

    // 1. 新增帖子，接收 AddPost 对象
    @PostMapping("/addPost")
    public BaseResponse addPost(@RequestBody AddPost addPost) {
        try {
            if (addPost == null || addPost.getAddId() == null || addPost.getTitle() == null
                    || addPost.getContent() == null)
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
    public BaseResponse deletePost(@RequestParam Long postId, @RequestParam Long userId) {
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
            if (updatePost == null || updatePost.getPostId() == null || updatePost.getUserId() == null
                    || updatePost.getContent() == null)
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

    // 4. 查询所有帖子的粗略信息，返回分页数据（传统分页）
    @GetMapping("/getPostList")
    @Deprecated
    public BaseResponse<Page<PostInfo>> getPostList(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            // Pass keyword to service call
            Page<Post> postPage = postService.getPostList(pageNum, pageSize, keyword);

            // 创建一个新的 Page<PostInfo> 用于返回，复制分页信息
            Page<PostInfo> postInfoPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
            postInfoPage.setPages(postPage.getPages()); // 设置总页数

            // 将 List<Post> 转换为 List<PostInfo>
            List<PostInfo> postInfoList = postPage.getRecords().stream()
                    .map(Post::toPostInfo) // 使用 Post 类中的转换方法
                    .collect(Collectors.toList());

            // 设置转换后的记录列表
            postInfoPage.setRecords(postInfoList);

            // 返回成功响应，包含分页数据
            return ResultUtils.success(postInfoPage);
        } catch (Exception e) {
            log.error("分页查询帖子列表异常", e);
            // 注意：根据你的 BaseResponse 结构，可能需要调整错误返回方式
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询帖子列表失败！");
        }
    }
    
    // 使用游标分页查询所有帖子
    @GetMapping("/getPostListWithCursor")
    public BaseResponse<CursorPageResponse<PostInfo>> getPostListWithCursor(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean asc,
            @RequestParam(required = false) String keyword) {
        try {
            CursorPageRequest cursorRequest = new CursorPageRequest();
            cursorRequest.setCursor(cursor);
            cursorRequest.setPageSize(pageSize);
            cursorRequest.setAsc(asc);
            
            CursorPageResponse<Post> postPage = postService.getPostListWithCursor(cursorRequest, keyword);
            
            // 将Post列表转换为PostInfo列表
            List<PostInfo> postInfoList = postPage.getRecords().stream()
                    .map(Post::toPostInfo)
                    .collect(Collectors.toList());
            
            // 构建PostInfo的游标分页响应
            CursorPageResponse<PostInfo> response = CursorPageResponse.build(
                    postInfoList, 
                    postPage.getNextCursor(), 
                    postPage.getHasMore()
            );
            
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("游标分页查询帖子列表异常", e);
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

    // 获取点赞数最高的帖子
    @GetMapping("/posts/top")
    public BaseResponse getTopPost() {
        try {
            Post topPost = postService.getTopPost();
            if (topPost == null) {
                return ResultUtils.error(ErrorCode.POST_NOT_FOUND, "没有帖子数据");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("id", topPost.getId());
            result.put("title", topPost.getTitle());
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("查询点赞最高帖子异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "查询点赞最高帖子失败");
        }
    }

    // 6. 帖子点赞，放redis里，弄异步任务，一分钟后删除，同时更新点赞数
    @PostMapping("/likePost")
    public BaseResponse likePost(@RequestParam Long postId) {
        try {
            if (postId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数为空！");
            }
            boolean result = likesUtil.like(POST_TYPE, postId);
            if (!result)
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞失败！");

            return ResultUtils.success("点赞成功！");
        } catch (Exception e) {
            log.error("点赞帖子异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "点赞失败！");
        }
    }

    // 7. 获取用户个人发布的帖子列表，支持分页（传统分页）
    @PostMapping("/getMyPosts")
    @Deprecated
    public BaseResponse getMyPosts(@RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (userId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空！");
            }

            // 调用service层方法获取用户帖子
            List<PostInfo> myPosts = postService.getPostsByUserId(userId, pageNum, pageSize);

            if (myPosts == null) {
                return ResultUtils.error(ErrorCode.POST_NOT_FOUND, "未找到帖子！");
            }

            // 构建返回结果，包含帖子列表和分页信息
            Map<String, Object> result = new HashMap<>();
            result.put("posts", myPosts);

            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("获取用户帖子列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取用户帖子列表失败！");
        }
    }
    
    // 使用游标分页获取用户个人发布的帖子列表
    @PostMapping("/getMyPostsWithCursor")
    public BaseResponse<CursorPageResponse<PostInfo>> getMyPostsWithCursor(
            @RequestParam Long userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean asc) {
        try {
            if (userId == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空！");
            }

            CursorPageRequest cursorRequest = new CursorPageRequest();
            cursorRequest.setCursor(cursor);
            cursorRequest.setPageSize(pageSize);
            cursorRequest.setAsc(asc);
            
            // 调用service层方法获取用户帖子（游标分页）
            CursorPageResponse<PostInfo> response = postService.getPostsByUserIdWithCursor(userId, cursorRequest);
            
            return ResultUtils.success(response);
        } catch (Exception e) {
            log.error("游标分页获取用户帖子列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取用户帖子列表失败！");
        }
    }
}
