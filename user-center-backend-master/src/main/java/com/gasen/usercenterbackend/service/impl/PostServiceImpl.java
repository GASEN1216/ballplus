package com.gasen.usercenterbackend.service.impl;

import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.model.respond.PostDetail;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.service.IPostService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PostServiceImpl implements IPostService {
    @Resource
    private PostMapper postMapper;

    @Override
    public boolean addPost(Post post) {
        return postMapper.insert(post) > 0;
    }

    @Override
    public PostDetail getPostDetail(Long postId) {
        try {
            Post post = postMapper.selectById(postId);
            return post.toPostDetail();
        } catch (Exception e) {
            log.error("查询帖子详情异常", e);
            return null;
        }
    }

    @Override
    public boolean deletePost(Long postId, Integer userId) {
        try {
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return false;
            }
            if (post.getAppId().equals(userId)) {
                // 逻辑删除
                post.setIsDelete((byte) 1);
                return true;
            } else {
                log.error("删除帖子异常，用户id不匹配");
                return false;
            }
        } catch (Exception e) {
            log.error("删除帖子异常", e);
            return false;
        }
    }

    @Override
    public boolean updatePost(Post post) {
        try {
            Post oldPost = postMapper.selectById(post.getId());
            if (oldPost == null) {
                log.error("更新帖子异常，帖子不存在");
                return false;
            }
            if (!oldPost.getAppId().equals(post.getAppId())) {
                log.error("更新帖子异常，用户id不匹配");
                return false;
            }
        } catch (Exception e) {
            log.error("更新帖子异常", e);
            return false;
        }
        return postMapper.updateById(post) > 0;
    }

    @Override
    public List<Post> getPostList() {
        try {
            return postMapper.selectList(null);
        } catch (Exception e) {
            log.error("查询帖子列表异常", e);
            return null;
        }
    }

    @Override
    public boolean reduceComments(Long postId) {
        try {
            Post post = postMapper.selectById(postId);
            if (post == null) {
                log.error("减少帖子评论异常，帖子不存在");
                return false;
            }
            post.setComments(post.getComments() - 1);
            return postMapper.updateById(post) > 0;
        } catch (Exception e) {
            log.error("减少帖子评论异常", e);
            return false;
        }
    }
}
