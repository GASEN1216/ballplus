package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.respond.PostDetail;
import com.gasen.usercenterbackend.model.dao.Post;

import java.util.List;

public interface IPostService {
    boolean addPost(Post post);

    PostDetail getPostDetail(Long postId);

    boolean deletePost(Long postId, Integer userId);

    boolean updatePost(Post post);

    List<Post> getPostList();

    boolean reduceComments(Long postId);
}
