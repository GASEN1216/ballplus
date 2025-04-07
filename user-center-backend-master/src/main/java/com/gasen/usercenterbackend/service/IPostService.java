package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.vo.PostDetail;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.vo.PostInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;

import java.util.List;

public interface IPostService extends ILikesService {
    boolean addPost(Post post);

    PostDetail getPostDetail(Long postId);

    boolean deletePost(Long postId, Long userId);

    boolean updatePost(Post post);

    /**
     * 分页获取帖子列表（传统分页）
     * @deprecated 使用 getPostListWithCursor 替代
     */
    @Deprecated
    Page<Post> getPostList(long pageNum, long pageSize, String keyword);
    
    /**
     * 使用游标分页获取帖子列表
     * 
     * @param cursorRequest 游标分页请求
     * @param keyword 关键字
     * @return 游标分页响应
     */
    CursorPageResponse<Post> getPostListWithCursor(CursorPageRequest cursorRequest, String keyword);

    boolean reduceComments(Long postId);

    boolean addComments(Long postId);

    /**
     * 获取点赞数最高的帖子
     *
     * @return 点赞数最高的帖子
     */
    Post getTopPost();

    /**
     * 根据用户ID获取该用户发布的帖子列表（传统分页）
     * @deprecated 使用 getPostsByUserIdWithCursor 替代
     */
    @Deprecated
    List<PostInfo> getPostsByUserId(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 使用游标分页获取用户发布的帖子列表
     * 
     * @param userId 用户ID
     * @param cursorRequest 游标分页请求
     * @return 游标分页响应
     */
    CursorPageResponse<PostInfo> getPostsByUserIdWithCursor(Long userId, CursorPageRequest cursorRequest);
}
