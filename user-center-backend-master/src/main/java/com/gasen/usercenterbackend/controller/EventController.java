package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.Event;
import com.gasen.usercenterbackend.model.respond.indexEvent;
import com.gasen.usercenterbackend.service.IEventService;
import com.gasen.usercenterbackend.service.IUserEventService;
import com.gasen.usercenterbackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "分页获取活动信息")
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
}
