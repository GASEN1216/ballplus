package com.gasen.usercenterbackend.config.rabbitmq;

import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.model.dto.LikeEvent;
import com.gasen.usercenterbackend.service.ICommentService;
import com.gasen.usercenterbackend.service.ILikesService;
import com.gasen.usercenterbackend.service.IPostService;
import com.gasen.usercenterbackend.service.ISubCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Component;

import static com.gasen.usercenterbackend.constant.LikeConstant.*;

@Component
@Slf4j
public class LikeEventConsumer {

    @Resource
    private IPostService postService;

    @Resource
    private ICommentService commentService;

    @Resource
    private ISubCommentService subCommentService;

    /**
     * 处理点赞事件
     * 使用自定义的线程池处理消息
     * @param event 点赞事件
     */
    @RabbitListener(queues = "likeQueue", containerFactory = "rabbitListenerContainerFactory")
    public void handleLikeEvent(LikeEvent event) {
        log.info("收到点赞事件，类型: {}, ID: {}, 当前线程: {}", event.getType(), event.getId(), Thread.currentThread().getName());
        
        if (event==null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞事件为空");

        ILikesService likesService = switch (event.getType()) {
            case POST_TYPE -> postService;
            case COMMENT_TYPE -> commentService;
            case SUB_COMMENT_TYPE -> subCommentService;
            default -> throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "点赞事件类型错误");
        };

        if(!likesService.updateLikes(event.getId(), event.getLikes())){
            throw new BusinessExcetion(ErrorCode.UPDATE_POST_LIKES_FAILED, "点赞失败");
        }
        log.info("成功处理点赞事件，type: {}, id: {}, likes: {}", event.getType(), event.getId(), event.getLikes());
    }
}
