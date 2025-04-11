package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dto.DashboardStatsDTO;
import com.gasen.usercenterbackend.model.dto.UserTrendResponseDTO;
import com.gasen.usercenterbackend.model.dto.ContentTrendResponseDTO;
import com.gasen.usercenterbackend.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private IDashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/stats")
    public BaseResponse<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResultUtils.success(stats);
    }

    @Operation(summary = "获取用户趋势数据")
    @GetMapping("/user-trend")
    public BaseResponse<UserTrendResponseDTO> getUserTrend() {
        UserTrendResponseDTO trend = dashboardService.getUserTrend();
        return ResultUtils.success(trend);
    }

    @Operation(summary = "获取内容趋势数据")
    @GetMapping("/content-trend")
    public BaseResponse<ContentTrendResponseDTO> getContentTrend() {
        ContentTrendResponseDTO trend = dashboardService.getContentTrend();
        return ResultUtils.success(trend);
    }
} 