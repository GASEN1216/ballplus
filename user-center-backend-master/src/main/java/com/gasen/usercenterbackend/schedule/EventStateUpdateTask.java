package com.gasen.usercenterbackend.schedule;

import com.gasen.usercenterbackend.model.dao.Event;
import com.gasen.usercenterbackend.service.IEventService;
import com.gasen.usercenterbackend.utils.WechatUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class EventStateUpdateTask {

    @Resource
    private IEventService eventService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

    /**
     * 每分钟运行一次，更新 event 表中 eventDate 早于现在的活动状态
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void updateEventState() {
        int count = eventService.scheduleUpdateEventState();
        if (count > 0) {
            log.info("更新了 {} 条活动状态", count);
        } else {
            log.info("没有活动需要更新");
        }
    }

    /**
     * 每半小时运行一次，检查 Redis ZSET 中的活动 ID，发送通知
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkAndSendEventNotifications() {
        String zsetKey = "ballplus:events:sorted";
        long currentTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long oneHourInSeconds = 60 * 60; // 一小时的秒数

        // 获取 ZSET 中所有时间戳小于等于当前时间 + 一小时的活动 ID
        Set<String> eventIds = stringRedisTemplate.opsForZSet().rangeByScore(zsetKey, 0, currentTimestamp + oneHourInSeconds);

        if (eventIds == null || eventIds.isEmpty()) {
            log.info("没有即将开始的活动需要通知");
            return;
        }

        for (String eventId : eventIds) {
            Event event = eventService.getById(Long.parseLong(eventId));
            if (event == null) {
                log.error("活动 ID {} 对应的活动不存在", eventId);
                continue;
            }
            // 获取活动的时间戳
            Double timestamp = stringRedisTemplate.opsForZSet().score(zsetKey, eventId);
            if (timestamp == null) {
                log.error("活动 ID {} 的时间戳为空", eventId);
                continue;
            }

            // 检查时间戳是否与当前时间相差不到一小时
            if (timestamp - currentTimestamp <= oneHourInSeconds) {
                // 从 ZSET 中移除该活动 ID
                stringRedisTemplate.opsForZSet().remove(zsetKey, eventId);

                // 从 Redis List 中获取所有 openid
                String listKey = "ballplus:events:" + eventId;
                List<String> openIds = stringRedisTemplate.opsForList().range(listKey, 0, -1);

                if (openIds != null && !openIds.isEmpty()) {
                    // 删除 Redis List
                    stringRedisTemplate.delete(listKey);

                    // 提交任务到线程池，异步发送通知
                    threadPoolExecutor.execute(() -> {
                        try {
                            // 调用 WechatUtil 发送通知
                            boolean success = WechatUtil.sendEventStartNotification(openIds, event);
                            if (!success) {
                                log.error("eventId: {} 的活动开始通知未全部发送", eventId);
                            }
                        } catch (Exception e) {
                            // 记录日志或处理异常
                            log.error("线程{}发送活动开始通知失败", Thread.currentThread().getName(), e);
                        }
                    });
                }
            }
        }
    }
}