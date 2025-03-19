package com.gasen.usercenterbackend.service.impl;

import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.PostMapper;
import com.gasen.usercenterbackend.model.dao.Post;
import com.gasen.usercenterbackend.model.vo.CommentDetail;
import com.gasen.usercenterbackend.model.vo.PostDetail;
import com.gasen.usercenterbackend.model.vo.SubCommentDetail;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Service
@Slf4j
public class PostServiceImpl implements IPostService {
    @Resource
    private PostMapper postMapper;

    @Resource
    private ICommentService commentService;

    @Resource
    private ISubCommentService subCommentService;

    @Override
    public boolean addPost(Post post) {
        return postMapper.insert(post) > 0;
    }

    // TODO：暂时全部拿取，后续优化
    @Override
    public PostDetail getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            log.error("查询帖子详情异常，帖子不存在");
            return null;
        }
        PostDetail postDetail = post.toPostDetail();
        // 查询主评论
        List<CommentDetail> commentList = commentService.getCommentsByPostId(postId);

        for (CommentDetail comment : commentList) {
            // 查询该评论的子评论
            List<SubCommentDetail> subComments = subCommentService.getSubCommentsByCommentId(comment.getCommentId());
            comment.setSubComments(subComments);
        }
        postDetail.setCommentsList(commentList);

        return postDetail;
    }


    @Override
    public boolean deletePost(Long postId, Integer userId) {

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

    }

    @Override
    public boolean updatePost(Post post) {

        Post oldPost = postMapper.selectById(post.getId());
        if (oldPost == null) {
            log.error("更新帖子异常，帖子不存在");
            return false;
        }
        if (!oldPost.getAppId().equals(post.getAppId())) {
            log.error("更新帖子异常，用户id不匹配");
            return false;
        }

        return postMapper.updateById(post) > 0;
    }

    @Override
    public List<Post> getPostList() {
        return postMapper.selectList(null);
    }

    @Override
    public boolean reduceComments(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            log.error("减少帖子评论异常，帖子不存在");
            return false;
        }
        post.setComments(post.getComments() - 1);
        return postMapper.updateById(post) > 0;
    }

    @Override
    public boolean addComments(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            log.error("增加帖子评论异常，帖子不存在");
            return false;
        }
        post.setComments(post.getComments() + 1);
        return postMapper.updateById(post) > 0;
    }

    @Override
    public Integer getLikesById(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "帖子不存在");
        return post.getLikes();
    }

    @Override
    public boolean updateLikes(Long postId, Integer likes) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "帖子不存在");
        }
        if (likes == null || likes < 0)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞数错误");
        post.setLikes(likes);
        return postMapper.updateById(post) > 0;
    }
    
    @Override
    public Post getTopPost() {
        try {
            LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Post::getLikes);
            wrapper.last("LIMIT 1");
            return postMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.error("获取点赞最高帖子异常", e);
            return null;
        }
    }
}
