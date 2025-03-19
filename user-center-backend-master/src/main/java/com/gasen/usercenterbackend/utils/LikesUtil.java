package com.gasen.usercenterbackend.utils;

import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.ILikesService;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.gasen.usercenterbackend.constant.LikeConstant.*;

/**
 * 点赞工具类
 * @author gaosen
 * @date 2025/3/14
 */

@Slf4j
@Component
public class LikesUtil {

    @Resource
    private RedisTemplate<String, Long> longRedisTemplate;

    @Resource
    private IPostService postService;

    @Resource
    private ICommentService commentService;

    @Resource
    private ISubCommentService subCommentService;

    public boolean like(int type, Long id) {
        if(!ifTypeValid(type) || id == null){
            log.error("点赞失败，type或id错误: {}, id: {}", type, id);
        }
        String prefix;
        ILikesService likesService;
        switch (type){
            case 0: prefix = REDIS_POST_LIKES; likesService = postService; break;
            case 1: prefix = REDIS_COMMENT_LIKES; likesService = commentService; break;
            case 2: prefix = REDIS_SUB_COMMENT_LIKES; likesService = subCommentService; break;
            default: return false;
        }

        // 先检查redis里有没有这个键
        String likeCountKey = prefix + id;
        String copyLikeCountKey = COPY + likeCountKey;
        if (Boolean.FALSE.equals(longRedisTemplate.hasKey(likeCountKey))) {
            Long likes = likesService.getLikesById(id).longValue();
            longRedisTemplate.opsForValue().set(likeCountKey, likes);
            longRedisTemplate.opsForValue().set(copyLikeCountKey, Long.valueOf(0), Duration.ofSeconds(EXPIRE_TIME));
        }

        // Redis 自增点赞数
        Long newLikes = longRedisTemplate.opsForValue().increment(likeCountKey, 1);

        return newLikes != null;
    }

    public boolean ifTypeValid(int type) {
        return type >= 0 && type <= 2;
    }

    public int getTypeByPrefix(String prefix) {
        return switch (prefix) {
            case "post" -> POST_TYPE;
            case "comment" -> COMMENT_TYPE;
            case "subcomment" -> SUB_COMMENT_TYPE;
            default -> -1;
        };
    }
}
