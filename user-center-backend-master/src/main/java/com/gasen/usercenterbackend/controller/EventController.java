package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.mapper.EventMapper;
import com.gasen.usercenterbackend.model.dao.Event;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dao.UserEvent;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.model.vo.detailEvent;
import com.gasen.usercenterbackend.model.vo.eventTemplates;
import com.gasen.usercenterbackend.model.vo.indexEvent;
import com.gasen.usercenterbackend.model.vo.indexEventWithState;
import com.gasen.usercenterbackend.model.userIdAndAvatar;
import com.gasen.usercenterbackend.service.IEventService;
import com.gasen.usercenterbackend.service.IUserEventService;
import com.gasen.usercenterbackend.service.IUserService;
import com.gasen.usercenterbackend.service.IEventCancelRecordService;
import com.gasen.usercenterbackend.service.ICreditRecordService;
import com.gasen.usercenterbackend.utils.WechatUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class EventController {
    @Resource
    private IUserService userService;

    @Resource
    private IEventService eventService;

    @Resource
    private IUserEventService userEventService;

    @Resource
    private IEventCancelRecordService eventCancelRecordService;

    @Resource
    private ICreditRecordService creditRecordService;

    @Autowired
    private EventMapper eventMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    @Qualifier("stringRedisTemplate") // 指定使用名为 longRedisTemplate 的 Bean
    private RedisTemplate<String, String> stringRedisTemplate;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Operation(summary = "创建一个新的活动")
    @PostMapping("/createEvent")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse createEvent(@RequestBody Event event) {
        // 创建活动
        Long eventId = eventService.createEvent(event);
        Boolean isSuccess = userEventService.createUserEvent(event.getAppId(), eventId);

        if (!isSuccess) {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "创建活动失败！");
        }

        // 计算时间戳（基于 event.date 和 event.time）
        String dateTimeString = event.getEventDate() + " " + event.getEventTime(); // 拼接日期和时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime eventDateTime = LocalDateTime.parse(dateTimeString, formatter);
        long timestamp = eventDateTime.toEpochSecond(ZoneOffset.UTC); // 转换为时间戳（秒）

        String redisKey = "ballplus:events:sorted";

        redisTemplate.opsForZSet().add(redisKey, eventId, timestamp);

        return ResultUtils.success(eventId);
    }

    @Operation(summary = "取消一个活动")
    @PostMapping("/cancelEvent")
    public BaseResponse cancelEvent(Long userId, Long eventId, String cancelReason) {
        if (userId == null || eventId == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        }
        
        // 获取活动详情，用于发送通知
        Event event = eventService.getById(eventId);
        if (event == null) {
            return ResultUtils.error(ErrorCode.NO_EVENTS, "活动不存在");
        }
        
        // 验证用户是否是活动创建者
        if (!event.getAppId().equals(userId)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "非活动创建者不能取消活动");
        }
        
        // 获取参与活动的所有用户
        List<User> participants = userEventService.getEventParticipants(eventId);
        
        // 保存取消原因
        eventCancelRecordService.saveEventCancelRecord(eventId.longValue(), cancelReason);
        
        // 检查是否在活动开始前半小时内取消，如果是则扣除信誉分
        boolean deductCredit = false;
        try {
            // 获取活动日期和时间
            LocalDate eventDate = event.getEventDate();
            String eventTime = String.valueOf(event.getEventTime());

            // 解析活动时间
            String[] timeParts = eventTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // 创建活动开始的日期时间
            LocalDateTime eventStartDateTime = LocalDateTime.of(
                    eventDate.getYear(),
                    eventDate.getMonthValue(),
                    eventDate.getDayOfMonth(),
                    hour,
                    minute
            );
            
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            
            // 如果当前时间在活动开始前半小时内，则扣除信誉分
            if (now.isAfter(eventStartDateTime.minusMinutes(30)) && now.isBefore(eventStartDateTime)) {
                deductCredit = true;
            }
        } catch (Exception e) {
            log.error("检查活动时间失败", e);
        }
        
        // 取消活动
        if (eventService.cancelEvent(userId, eventId)){
            // 从Redis中删除活动相关数据
            String redisKey = "ballplus:events:" + eventId;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                stringRedisTemplate.delete(redisKey);
            }
            String redisKey2 = "ballplus:events:sorted";
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey2))) {
                redisTemplate.delete(redisKey2);
            }
            
            // 如果在活动开始前半小时内取消，扣除10分信誉分
            if (deductCredit) {
                creditRecordService.addCreditRecord(userId, -2, 2, "在活动开始前半小时内取消活动", eventId);
                
                userService.updateCredit(userId, -2);
                
                log.info("用户ID: {} 在活动开始前半小时内取消活动，扣除2分信誉分", userId);
            }
            
            // 异步发送取消通知
            if (participants != null && !participants.isEmpty()) {
                threadPoolExecutor.execute(() -> {
                    try {
                        List<String> openidList = participants.stream()
                                .map(User::getOpenId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        
                        if (!openidList.isEmpty()) {
                            boolean success = WechatUtil.sendEventCancelNotification(openidList, event, cancelReason);
                            if (!success) {
                                log.error("活动ID: {} 的取消通知未全部发送成功", eventId);
                            }
                        }
                    } catch (Exception e) {
                        log.error("发送活动取消通知失败", e);
                    }
                });
            }
            
            return ResultUtils.success("取消活动成功！");
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"取消活动失败！");
    }

    @Operation(summary = "退出活动")
    @PostMapping("/quitEvent")
    @Transactional
    public BaseResponse quitEvent(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "eventId") Long eventId){
        if (userId == null || eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
            
        // 获取活动详情
        Event event = eventService.getById(eventId);
        if (event == null) {
            return ResultUtils.error(ErrorCode.NO_EVENTS, "活动不存在");
        }
        
        // 检查是否在活动开始前半小时内退出，如果是则扣除信誉分
        boolean deductCredit = false;
        try {
            // 获取活动日期和时间
            LocalDate eventDate = event.getEventDate();
            String eventTime = String.valueOf(event.getEventTime());
            
            // 解析活动时间
            String[] timeParts = eventTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // 创建活动开始的日期时间
            LocalDateTime eventStartDateTime = LocalDateTime.of(
                    eventDate.getYear(), 
                    eventDate.getMonthValue(), 
                    eventDate.getDayOfMonth(), 
                    hour, 
                    minute
            );
            
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            
            // 如果当前时间在活动开始前半小时内，则扣除信誉分
            if (now.isAfter(eventStartDateTime.minusMinutes(30)) && now.isBefore(eventStartDateTime)) {
                deductCredit = true;
            }
        } catch (Exception e) {
            log.error("检查活动时间失败", e);
        }
        
        if (userEventService.quitEvent(userId, eventId) && eventService.reduceParticipants(eventId)){
            String redisKey = "ballplus:events:" + eventId;
            // 先查询redis里有没有这个键名，如果没有就没事，如果有就删除list里的openid
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                String openid = userService.getOpenIdByUserId(userId);
                stringRedisTemplate.opsForList().remove(redisKey, 0, openid);
            }
            
            // 如果在活动开始前半小时内退出，扣除5分信誉分
            if (deductCredit) {
                creditRecordService.addCreditRecord(userId, -1, 2, "退出活动", eventId);

                userService.updateCredit(userId, -1);

                log.info("用户ID: {} 活动开始前半小时内退出活动，扣除1分信誉分", userId);
            }
            
            return ResultUtils.success("退出活动成功！");
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"退出活动失败！");
    }

    @Operation(summary = "参加活动")
    @PostMapping("/joinEvent")
    @Transactional
    public BaseResponse joinEvent(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "eventId") Long eventId){
        if (userId == null || eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        if (userEventService.createUserEvent(userId, eventId) && eventService.addParticipants(eventId))
            return ResultUtils.success("参加活动成功！");

        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"参加活动失败！");
    }

    /**
     * 分页获取
     */
//    @Operation(summary = "列表模式分页获取活动信息")
//    @GetMapping("/getEvent")
//    public BaseResponse getEvent(
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @RequestParam(value = "size", defaultValue = "5") int size) {
//        try {
//            // 使用 MyBatis-Plus 的分页功能获取活动数据
//            Page<Event> eventPage = new Page<>(page, size);
//            IPage<Event> eventIPage = eventService.page(eventPage);
//
//            // 对查询结果的每个 Event 对象执行 toindexEvent
//            List<indexEvent> res = eventIPage.getRecords().stream()
//                    .map(Event::toIndexEvent)
//                    .collect(Collectors.toList());
//
//            return ResultUtils.success(res);
//        } catch (Exception e) {
//            log.error("获取活动信息失败: {}", e.getMessage(), e);
//            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
//        }
//    }

    @Operation(summary = "列表模式获取全部活动信息")
    @GetMapping("/getEvent")
    public BaseResponse getEvent() {
        try {
            List<Event> events = eventService.getAllEvents();

            // 对查询结果的每个 Event 对象执行 toindexEvent
            List<indexEvent> res = events.stream()
                    .map(Event::toIndexEvent)
                    .collect(Collectors.toList());

            return ResultUtils.success(res);
        } catch (Exception e) {
            log.error("获取活动信息失败: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
        }
    }

    @Operation(summary = "管理员列表模式获取全部活动信息")
    @GetMapping("/admin/getEvent")
    public BaseResponse admingetEvent() {
        try {
            List<Event> events = eventService.getAllEvents();

            return ResultUtils.success(events);
        } catch (Exception e) {
            log.error("获取活动信息失败: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
        }
    }

    @Operation(summary = "地图模式获取活动信息")
    @PostMapping("/getEventByMap")
    public BaseResponse getEventByMap(@RequestParam(value = "location") String location) {
        try {
            if (location == null)
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR);

            // 获取当前地点的活动
            QueryWrapper<Event> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("location", location).eq("state", 0).ge("event_date", LocalDate.now());
            List<Event> events = eventMapper.selectList(queryWrapper);

            // 对查询结果的每个 Event 对象执行 toindexEvent
            List<indexEvent> res = events.stream()
                    .map(Event::toIndexEvent)
                    .collect(Collectors.toList());

            return ResultUtils.success(res);
        } catch (Exception e) {
            log.error("获取活动信息失败: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
        }
    }

    /*
    *  分页获取个人所有活动信息
    * */
//    @Operation(summary = "获取个人活动信息")
//    @PostMapping("/getEventByPer")
//    public BaseResponse getEventByPer(
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @RequestParam(value = "size", defaultValue = "5") int size,
//            @RequestParam(value = "userId") Integer userId){
//        try {
//            if (userId == null)
//                return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
//            // 使用 MyBatis-Plus 的分页功能获取活动数据
//            Page<UserEvent> userEventPage = new Page<>(page, size);
//            QueryWrapper<UserEvent> queryWrapper = new QueryWrapper<>();
//            queryWrapper.eq("user_id", userId);
//
//            IPage<UserEvent> userEventIPage = userEventService.pageByUserId(userEventPage, queryWrapper);
//
//            List<Long> eventIdLists = userEventIPage.getRecords().stream().map(UserEvent::getEventId).toList();
//            if(eventIdLists.isEmpty()) return ResultUtils.success(null);
//
//            List<Event> events = eventService.listByIds(eventIdLists);
//
//            // 对查询结果的每个 Event 对象执行 toindexEvent
//            List<indexEventWithState> res = events.stream()
//                    .map(Event::toIndexEventWithState)
//                    .collect(Collectors.toList());
//
//            return ResultUtils.success(res);
//        } catch (Exception e) {
//            log.error("获取活动信息失败: {}", e.getMessage(), e);
//            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
//        }
//    }

    @Operation(summary = "获取个人活动信息")
    @PostMapping("/getEventByPer")
    public BaseResponse getEventByPer(@RequestParam(value = "userId") Long userId){
        try {
            if (userId == null)
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
            List<Long> eventIds = userEventService.getAllEventIdsByUserId(userId);

            if(eventIds.isEmpty()) return ResultUtils.success(null);

            List<Event> events = eventService.listByIds(eventIds);

            // 对查询结果的每个 Event 对象执行 toindexEvent
            List<indexEventWithState> res = events.stream()
                    .map(Event::toIndexEventWithState)
                    .collect(Collectors.toList());

            return ResultUtils.success(res);
        } catch (Exception e) {
            log.error("获取活动信息失败: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
        }
    }

    @Operation(summary = "获取个人活动模板")
    @PostMapping("/getTemplateByPer")
    public BaseResponse getTemplateByPer(@RequestParam(value = "userId") Long userId){
        if (userId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        List<Event> events = eventService.findTemplates(userId);
        if(events.isEmpty())
            return ResultUtils.success(null);

        List<eventTemplates> res = events.stream().map(Event::toEventTemplates).toList();
        return ResultUtils.success(res);
    }

    @Operation(summary = "删除个人活动模板")
    @PostMapping("/deleteTemplateByPer")
    public BaseResponse deleteTemplateByPer(@RequestParam(value = "userId") Long userId,
                                            @RequestParam(value = "templateId") Long templateId){
        if (userId == null || templateId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        Boolean isSuccess = eventService.deleteTemplateByPer(userId, templateId);
        if(!isSuccess)
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"删除活动模板失败！");
        return ResultUtils.success("删除活动模板成功！");
    }

    @Operation(summary = "获取个人最近的一次活动")
    @PostMapping("/getNearestEvent")
    public BaseResponse getNearestEvent(@RequestParam(value = "userId") Long userId){
        if (userId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);

        List<Long> eventIds = userEventService.getAllEventIdsByUserId(userId);
        if(eventIds.isEmpty())
            return ResultUtils.success("");

        Event event = eventService.getNearestEvent(eventIds);

        if (event == null)
            return ResultUtils.success("");

        List<indexEvent> res = new ArrayList<>();
        res.add(event.toIndexEvent());
        return ResultUtils.success(res);
    }

    @Operation(summary = "获取自己所有参加的活动")
    @PostMapping("getAllMyEvents")
    public BaseResponse getAllMyEvents(@RequestParam(value = "userId") Long userId){
        if (userId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        List<Long> eventIds = userEventService.getAllEventIdsByUserId(userId);
        return ResultUtils.success(eventIds);
    }

    @Operation(summary = "获取活动详细数据")
    @GetMapping("/getEventDetailById")
    public BaseResponse getEventDetailById(@RequestParam(value = "eventId") Long eventId){
        if (eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        Event event = eventService.getById(eventId);
        if(event == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"活动不存在！");
        detailEvent res = event.toDetailEvent();
        // 找出所有参加这个活动的人
        List<Long> userIds = userEventService.getUserIdsByEventId(eventId);
        // 找出他们的头像url
        List<userIdAndAvatar> userIdAndAvatars = userService.getAvatarByUserIds(userIds);
        res.setPersons(userIdAndAvatars);
        return ResultUtils.success(res);
    }

    @Operation(summary = "匹配活动")
    @PostMapping("/matchEvent")
    public BaseResponse matchEvent(@RequestParam(value = "userId") Integer userId,
                                   @RequestParam(value = "latitude") Float latitude,
                                   @RequestParam(value = "longitude") Float longitude){
        if (userId == null || latitude == null || longitude == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);

        // 拿取个人信息
        User user = userService.getById(userId);

        // 匹配活动
        Long eventId = eventService.matchEvent(user, latitude, longitude);

        if(eventId == null)
            return ResultUtils.success("没有匹配的活动");

        return ResultUtils.success(eventId);
    }

    @Operation(summary = "活动报名成功通知")
    @PostMapping("/sendJoinEventNotification")
    public BaseResponse sendJoinEventNotification(@RequestParam(value = "userId") Long userId,
                                                  @RequestParam(value = "eventId") Long eventId) {
        if (userId == null || eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);

        // 获取用户 openid
        String openid = userService.getOpenIdByUserId(userId);
        if (openid == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户未绑定微信");

        // 获取活动信息
        Event event = eventService.getById(eventId);
        if (event == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "活动不存在");

        // 将 openid 存入 Redis List
        String redisKey = "ballplus:events:" + eventId;
        stringRedisTemplate.opsForList().rightPush(redisKey, openid);

        // 发送订阅消息
        boolean isSuccess = WechatUtil.sendJoinEventNotification(openid, event);
        if (isSuccess)
            return ResultUtils.success("活动报名成功通知已发送！");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "通知发送失败");
    }


    @Operation(summary = "活动即将开始通知")
    @PostMapping("/sendEventStartNotification")
    public BaseResponse sendEventStartNotification(@RequestParam(value = "eventId") Long eventId) {
        if (eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);

        Event event = eventService.getById(eventId);
        if (event == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "活动不存在");

        List<Long> useridList = userEventService.getUserIdsByEventId(eventId);

        if (useridList.isEmpty())
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "数据库错误");

        List<String> openidList = userService.getOpenIdByUserIds(useridList);

        if (openidList.isEmpty())
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"数据库错误");

        boolean isSuccess = WechatUtil.sendEventStartNotification(openidList, event);
        if (isSuccess)
            return ResultUtils.success("活动即将开始通知已发送！");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "通知发送失败");
    }

    @Operation(summary = "获取自己所有参加的活动（游标分页）")
    @PostMapping("/getUserEventsWithCursor")
    public BaseResponse getUserEventsWithCursor(
            @RequestParam(value = "userId") Long userId,
            @RequestBody CursorPageRequest cursorRequest) {
        if (userId == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空");
        }
        
        CursorPageResponse<UserEvent> response = userEventService.pageByUserIdWithCursor(userId, cursorRequest);
        return ResultUtils.success(response);
    }

}
