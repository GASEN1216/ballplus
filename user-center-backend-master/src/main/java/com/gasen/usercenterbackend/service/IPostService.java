package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.vo.PostDetail;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.vo.PostInfo;

import java.util.List;

public interface IPostService extends ILikesService {
    boolean addPost(Post post);

    PostDetail getPostDetail(Long postId);

    boolean deletePost(Long postId, Integer userId);

    boolean updatePost(Post post);

    List<Post> getPostList();

    boolean reduceComments(Long postId);

    boolean addComments(Long postId);

    /**
     * 获取点赞数最高的帖子
     *
     * @return 点赞数最高的帖子
     */
    Post getTopPost();

    /**
     * 根据用户ID获取该用户发布的帖子列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户发布的帖子列表
     */
    List<PostInfo> getPostsByUserId(Integer userId, Integer pageNum, Integer pageSize);
}
