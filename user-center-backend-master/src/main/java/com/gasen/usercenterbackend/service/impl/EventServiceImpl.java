package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.gasen.usercenterbackend.mapper.EventMapper;
import com.gasen.usercenterbackend.mapper.UserMapper;
import com.gasen.usercenterbackend.model.dao.Event;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.service.IEventService;
import com.gasen.usercenterbackend.service.IUserService;
import com.gasen.usercenterbackend.utils.WechatUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class EventServiceImpl implements IEventService {

    @Resource
    private EventMapper eventMapper;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private RedisTemplate<String, String> stringRedisTemplate;

    @Resource
    private IUserService userService;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Long createEvent(Event event) {
        event.setId(null);
        eventMapper.insert(event);
        return event.getId();
    }

    @Override
    public List<Event> listByIds(List<Long> eventIdLists) {
        return eventMapper.selectBatchIds(eventIdLists);
    }

    @Override
    public List<Event> findTemplates(Integer userId) {
        return eventMapper.selectList(new QueryWrapper<Event>().eq("app_id", userId).eq("is_template", 1));
    }

    @Override
    public Event getNearestEvent(List<Long> eventIds) {
        // 查询在eventIds里的所有活动，并且要求活动日期在当天之后，只返回一条最近的活动，都在当天比时间，时间都一样比id大小
        List<Event> events = eventMapper.selectList(new QueryWrapper<Event>().in("id", eventIds).eq("state", 0).ge("event_date", LocalDate.now()).orderByAsc("event_date").orderByAsc("event_time").orderByAsc("id"));
        return events.isEmpty() ? null : events.get(0);
    }

    @Override
    public Boolean deleteTemplateByPer(Integer userId, Integer templateId) {
        return eventMapper.update(new Event().setIsTemplate(false), new QueryWrapper<Event>().eq("app_id", userId).eq("id", templateId)) > 0;
    }

    @Override
    public Event getById(Long eventId) {
        return eventMapper.selectById(eventId);
    }

    @Override
    public boolean cancelEvent(Integer userId, Long eventId) {
        return eventMapper.update(new Event().setState((byte) 2), new QueryWrapper<Event>().eq("app_id", userId).eq("id", eventId)) > 0;
    }

    @Override
    public boolean reduceParticipants(Long eventId) {
        return eventMapper.update(new Event().setParticipants(eventMapper.selectById(eventId).getParticipants() - 1), new QueryWrapper<Event>().eq("id", eventId)) > 0;
    }

    @Override
    public boolean addParticipants(Long eventId) {
        return eventMapper.update(new Event().setParticipants(eventMapper.selectById(eventId).getParticipants() + 1), new QueryWrapper<Event>().eq("id", eventId)) > 0;
    }

    @Override
    public int scheduleUpdateEventState() {
        return eventMapper.update(new Event().setState((byte) 1), new QueryWrapper<Event>().eq("state", 0).apply("CONCAT(event_date, ' ', event_time) < NOW()"));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventMapper.selectList(new QueryWrapper<Event>().eq("state", 0).ge("event_date", LocalDate.now()));
    }

    /**
     * 匹配活动
     * 1. 先根据性别限制 (limits) 过滤
     * 2. 再根据地理位置计算距离
     * 3. 返回最近的活动 ID
     */
    @Override
    public Long matchEvent(User user, Float latitude, Float longitude) {
        // 获取所有未开始的活动
        QueryWrapper<Event> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("event_date", java.time.LocalDate.now()) // 只匹配未开始的活动
                .eq("state", 0);  // 只匹配状态为 0 (正常) 的活动

        List<Event> events = eventMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(events)) {
            return null; // 没有匹配到任何活动
        }

        // 1. 根据性别匹配
        List<Event> filteredEvents = events.stream()
                .filter(event -> isGenderMatch(event, user.getGender()))
                .toList();

        if (filteredEvents.isEmpty()) {
            return null; // 没有匹配的活动
        }

        // 2. 按地理位置排序
        Optional<Event> closestEvent = filteredEvents.stream()
                .min(Comparator.comparing(event -> calculateDistance(latitude, longitude, event.getLatitude().floatValue(), event.getLongitude().floatValue())));

        return closestEvent.map(Event::getId).orElse(null);
    }

    @Override
    public Boolean sendEventStartNotification(Long eventId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null) {
            log.error("活动id: {} 不存在", eventId);
            return false;
        }

        // 构建 Redis 列表的 key
        String redisKey = "ballplus:events:" + eventId;

        // 从 Redis 列表中获取所有 OpenID
        List<String> openIds = stringRedisTemplate.opsForList().range(redisKey, 0, -1);

        if (openIds == null || openIds.isEmpty()) {
            log.error("查找活动id: {} 的人失败，openids为空", eventId);
            return false;
        }

        // 删除 Redis 列表
        stringRedisTemplate.delete(redisKey);

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

        return true;
    }

    @Override
    public Boolean sendEventInOneHourStartNotification(Event event) {
        if (event == null) {
            log.error("活动不存在");
            return false;
        }

        Long eventId = event.getId();

        String openId = userService.getOpenIdByUserId(event.getAppId());

        if (openId == null || openId.isEmpty()) {
            log.error("查找id: {} 的人失败，openid为空", event.getAppId());
            return false;
        }

        List<String> openIds = new ArrayList<>();
        openIds.add(openId);

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

        return true;
    }

    /**
     * 计算用户性别和活动的匹配关系
     *
     * @param event      活动
     * @param userGender 用户性别 (0: 未知, 1: 女, 2: 男)
     * @return 是否匹配
     */
    private boolean isGenderMatch(Event event, Integer userGender) {
        Byte eventLimit = event.getLimits(); // 活动的性别限制
        if (eventLimit == 0) return true; // 0: 无限制
        if (userGender == null || userGender == 0) return false; // 未知性别的用户无法匹配
        return eventLimit == userGender.byteValue(); // 匹配 1-女 或 2-男
    }

    /**
     * 计算两个地理坐标点之间的距离（单位：公里）
     * 使用 Haversine 公式
     */
    private double calculateDistance(float lat1, float lon1, float lat2, float lon2) {
        final double R = 6371.0; // 地球半径 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // 返回距离（km）
    }

    /**
     * 判断活动是否已完成
     * @param eventId 活动ID
     * @return 是否已完成
     */
    @Override
    public boolean isEventCompleted(Long eventId) {
        Event event = getById(eventId);
        if (event == null) {
            return false;
        }
        
        // 活动状态为1表示已完成
        return event.getState() == 1;
    }

}
