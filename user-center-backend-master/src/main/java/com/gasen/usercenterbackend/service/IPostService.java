package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.vo.PostDetail;
import com.gasen.usercenterbackend.model.dao.Post;

import java.util.List;

public interface IPostService extends ILikesService{
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
}
