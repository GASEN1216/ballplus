package com.gasen.usercenterbackend.config.redis;

import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.config.rabbitmq.RabbitMQConfig;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.model.dto.LikeEvent;
import com.gasen.usercenterbackend.utils.LikesUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class RedisExpiredEventListener implements MessageListener {

    @Resource
    private RedisTemplate<String, Long> longRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    @Resource
    private LikesUtil likesUtil;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 拿到redis的key，去掉前面的copy:，再拿取新的key去读取redis, 得到点赞数，再分解key（最后一个:后的数字是postId）得到postId
        // 获取过期的 key 字符串
        String expiredKey = new String(message.getBody());
        // 示例 key 格式：copy:ballplus:post:likes:123
        if (expiredKey.startsWith("copy:")) {
            // 去掉前缀 "copy:"，得到 "ballplus:post:likes:123"
            String realKey = expiredKey.substring("copy:".length());

            // 分割 key，提取最后一部分作为 postId
            String[] parts = realKey.split(":");
            System.out.println("parts: " + Arrays.toString(parts));
            if (parts.length >= 4) {
                int type = likesUtil.getTypeByPrefix(parts[1]);

                Long id = Long.valueOf(parts[parts.length - 1]);

                // 从 Redis 中读取真实 key 的值，即点赞数
                Long newLikes = longRedisTemplate.opsForValue().get(realKey);
                if (newLikes == null) {
                    throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR, "从redis中获取点赞数失败！");
                }

                // 删除 Redis 中的 key
                longRedisTemplate.delete(realKey);

                // 构造点赞事件消息
                LikeEvent event = new LikeEvent(type, id, newLikes.intValue());

                // 发送消息到 RabbitMQ（这里不设置延迟头，因为我们已经利用 key 过期实现了延迟）
                rabbitTemplate.convertAndSend(
                        rabbitMQConfig.getLikeExchange(),
                        rabbitMQConfig.getLikeRoutingKey(),
                        event
                );
            }
        }
    }
}
