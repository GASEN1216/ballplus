package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.Event;
import com.gasen.usercenterbackend.model.UserEvent;
import com.gasen.usercenterbackend.model.respond.detailEvent;
import com.gasen.usercenterbackend.model.respond.eventTemplates;
import com.gasen.usercenterbackend.model.respond.indexEvent;
import com.gasen.usercenterbackend.model.respond.indexEventWithState;
import com.gasen.usercenterbackend.model.userIdAndAvatar;
import com.gasen.usercenterbackend.service.IEventService;
import com.gasen.usercenterbackend.service.IUserEventService;
import com.gasen.usercenterbackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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

    @Operation(summary = "创建一个新的活动")
    @PostMapping("/createEvent")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse createEvent(@RequestBody Event event) {
        Long eventId = eventService.createEvent(event);
        Boolean isSuccess = userEventService.createUserEvent(event.getAppId(), eventId);
        if(isSuccess)
            return ResultUtils.success("创建活动成功！");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"创建活动失败！");
    }

    @Operation(summary = "取消一个活动")
    @PostMapping("/cancelEvent")
    public BaseResponse cancelEvent(
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "eventId") Long eventId){
        if (userId == null || eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        if (eventService.cancelEvent(userId, eventId))
            return ResultUtils.success("取消活动成功！");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"取消活动失败！");
    }

    @Operation(summary = "退出活动")
    @PostMapping("/quitEvent")
    @Transactional
    public BaseResponse quitEvent(
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "eventId") Long eventId){
        if (userId == null || eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        if (userEventService.quitEvent(userId, eventId) && eventService.reduceParticipants(eventId))
            return ResultUtils.success("退出活动成功！");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"退出活动失败！");
    }

    @Operation(summary = "参加活动")
    @PostMapping("/joinEvent")
    @Transactional
    public BaseResponse joinEvent(
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "eventId") Long eventId){
        if (userId == null || eventId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        if (userEventService.createUserEvent(userId, eventId) && eventService.addParticipants(eventId))
            return ResultUtils.success("参加活动成功！");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"参加活动失败！");
    }

    @Operation(summary = "列表模式分页获取活动信息")
    @GetMapping("/getEvent")
    public BaseResponse getEvent(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        try {
            // 使用 MyBatis-Plus 的分页功能获取活动数据
            Page<Event> eventPage = new Page<>(page, size);
            IPage<Event> eventIPage = eventService.page(eventPage);

            // 对查询结果的每个 Event 对象执行 toindexEvent
            List<indexEvent> res = eventIPage.getRecords().stream()
                    .map(Event::toIndexEvent)
                    .collect(Collectors.toList());

            return ResultUtils.success(res);
        } catch (Exception e) {
            log.error("获取活动信息失败: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
        }
    }

    @Operation(summary = "地图模式分页获取活动信息")
    @PostMapping("/getEventByMap")
    public BaseResponse getEventByMap(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "location") String location) {
        try {
            if (location == null)
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
            // 使用 MyBatis-Plus 的分页功能获取活动数据
            Page<Event> eventPage = new Page<>(page, size);
            QueryWrapper<Event> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("location", location).eq("state", 0);

            IPage<Event> eventIPage = eventService.pageByLocation(eventPage, queryWrapper);

            // 对查询结果的每个 Event 对象执行 toindexEvent
            List<indexEvent> res = eventIPage.getRecords().stream()
                    .map(Event::toIndexEvent)
                    .collect(Collectors.toList());

            return ResultUtils.success(res);
        } catch (Exception e) {
            log.error("获取活动信息失败: {}", e.getMessage(), e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"获取活动信息失败，请稍后重试！");
        }
    }

    @Operation(summary = "获取个人活动信息")
    @PostMapping("/getEventByPer")
    public BaseResponse getEventByPer(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            @RequestParam(value = "userId") Integer userId){
        try {
            if (userId == null)
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
            // 使用 MyBatis-Plus 的分页功能获取活动数据
            Page<UserEvent> userEventPage = new Page<>(page, size);
            QueryWrapper<UserEvent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);

            IPage<UserEvent> userEventIPage = userEventService.pageByUserId(userEventPage, queryWrapper);

            List<Long> eventIdLists = userEventIPage.getRecords().stream().map(UserEvent::getEventId).toList();
            if(eventIdLists.isEmpty()) return ResultUtils.success(null);

            List<Event> events = eventService.listByIds(eventIdLists);

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
    public BaseResponse getTemplateByPer(@RequestParam(value = "userId") Integer userId){
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
    public BaseResponse deleteTemplateByPer(@RequestParam(value = "userId") Integer userId,
                                            @RequestParam(value = "templateId") Integer templateId){
        if (userId == null || templateId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        Boolean isSuccess = eventService.deleteTemplateByPer(userId, templateId);
        if(!isSuccess)
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"删除活动模板失败！");
        return ResultUtils.success("删除活动模板成功！");
    }

    @Operation(summary = "获取个人最近的一次活动")
    @PostMapping("/getNearestEvent")
    public BaseResponse getNearestEvent(@RequestParam(value = "userId") Integer userId){
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
    public BaseResponse getAllMyEvents(@RequestParam(value = "userId") Integer userId){
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
        List<Integer> userIds = userEventService.getUserIdsByEventId(eventId);
        // 找出他们的头像url
        List<userIdAndAvatar> userIdAndAvatars = userService.getAvatarByUserIds(userIds);
        res.setPersons(userIdAndAvatars);
        return ResultUtils.success(res);
    }

}
