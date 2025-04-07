package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dao.Complaint;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.vo.ComplaintVO;
import com.gasen.usercenterbackend.service.IComplaintService;
import com.gasen.usercenterbackend.service.IEventService;
import com.gasen.usercenterbackend.service.IUserEventService;
import com.gasen.usercenterbackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/wx/complaint")
public class ComplaintController {
    
    @Resource
    private IComplaintService complaintService;
    
    @Resource
    private IUserService userService;
    
    @Resource
    private IEventService eventService;
    
    @Resource
    private IUserEventService userEventService;

    @Operation(summary = "提交投诉")
    @PostMapping("/submit")
    @Transactional
    public BaseResponse submitComplaint(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "eventId") Long eventId,
            @RequestParam(value = "complainedIds") String complainedIds,
            @RequestParam(value = "content") String content) {
        
        // 参数校验
        if (userId == null || eventId == null || complainedIds == null || content == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数不能为空");
        }
        
        // 检查活动是否存在且已完成
        if (!isEventCompletedAndValid(eventId)) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "活动不存在或未完成");
        }
        
        // 检查用户是否参与了该活动
        if (!userEventService.isUserParticipated(userId, eventId)) {
            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, "您未参与该活动，无法提交投诉");
        }
        
        // 检查用户是否已经提交过投诉
        if (complaintService.hasComplaint(userId, eventId)) {
            return ResultUtils.error(ErrorCode.ALREADY_COMPLIANT, "您已经提交过投诉");
        }
        
        // 解析被投诉人ID
        List<Long> complainedIdList = Arrays.stream(complainedIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        if (complainedIdList.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "请选择被投诉人");
        }
        
        // 异步处理投诉
        complaintService.processComplaintsAsync(userId, eventId, content, complainedIdList);
        
        // 立即返回成功响应，不等待异步任务完成
        return ResultUtils.success("投诉提交成功，系统正在处理");
    }
    
    @Operation(summary = "获取活动的投诉记录")
    @GetMapping("/list")
    public BaseResponse getComplaintList(
            @RequestParam(value = "eventId") Long eventId) {
        
        if (eventId == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        }
        
        // 获取投诉记录
        List<Complaint> complaints = complaintService.getComplaintsByEventId(eventId);
        if (complaints.isEmpty()) {
            return ResultUtils.success(new ArrayList<>());
        }
        
        // 获取涉及的用户ID
        List<Long> userIds = new ArrayList<>();
        for (Complaint complaint : complaints) {
            userIds.add(complaint.getComplainerId());
            userIds.add(complaint.getComplainedId());
        }
        
        // 批量获取用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 转换为VO对象
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<ComplaintVO> result = complaints.stream()
                .map(complaint -> {
                    ComplaintVO vo = new ComplaintVO()
                            .setId(complaint.getId())
                            .setEventId(complaint.getEventId())
                            .setComplainerId(complaint.getComplainerId())
                            .setComplainedId(complaint.getComplainedId())
                            .setContent(complaint.getContent())
                            .setStatus(complaint.getStatus())
                            .setRejectReason(complaint.getRejectReason())
                            .setCreateTime(complaint.getCreateTime().format(formatter));
                    
                    // 设置投诉人信息
                    User complainer = userMap.get(complaint.getComplainerId());
                    if (complainer != null) {
                        vo.setComplainerName(complainer.getUserAccount())
                          .setComplainerAvatar(complainer.getAvatarUrl());
                    }
                    
                    // 设置被投诉人信息
                    User complained = userMap.get(complaint.getComplainedId());
                    if (complained != null) {
                        vo.setComplainedName(complained.getUserAccount())
                          .setComplainedAvatar(complained.getAvatarUrl());
                    }
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        return ResultUtils.success(result);
    }
    
    @Operation(summary = "检查用户是否可以提交投诉")
    @GetMapping("/check")
    public BaseResponse checkCanComplaint(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "eventId") Long eventId) {
        
        if (userId == null || eventId == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        }
        
        // 检查活动是否存在且已完成
        if (!isEventCompletedAndValid(eventId)) {
            return ResultUtils.success(false);
        }
        
        // 检查用户是否参与了该活动
        if (!userEventService.isUserParticipated(userId, eventId)) {
            return ResultUtils.success(false);
        }
        
        // 检查用户是否已经提交过投诉
        if (complaintService.hasComplaint(userId, eventId)) {
            return ResultUtils.success(false);
        }
        
        return ResultUtils.success(true);
    }
    
    @Operation(summary = "获取活动参与者（除了自己）")
    @GetMapping("/participants")
    public BaseResponse getEventParticipants(
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "eventId") Long eventId) {
        
        if (userId == null || eventId == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR);
        }
        
        // 获取活动参与者
        List<User> participants = userEventService.getEventParticipants(eventId);
        
        // 移除当前用户
        participants = participants.stream()
                .filter(user -> !user.getId().equals(userId))
                .toList();
        
        // 简化返回数据
        List<Map<String, ? extends Serializable>> result = participants.stream()
                .map(user -> Map.of(
                        "userId", user.getId(),
                        "userName", user.getUserAccount(),
                        "avatar", user.getAvatarUrl()
                ))
                .collect(Collectors.toList());
        
        return ResultUtils.success(result);
    }
    
    /**
     * 检查活动是否存在且已完成
     */
    private boolean isEventCompletedAndValid(Long eventId) {
        // 查询活动状态，判断是否已完成
        return eventService.isEventCompleted(eventId);
    }
}