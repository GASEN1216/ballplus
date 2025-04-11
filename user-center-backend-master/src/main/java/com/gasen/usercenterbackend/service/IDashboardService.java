package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dto.DashboardStatsDTO;
import com.gasen.usercenterbackend.model.dto.UserTrendResponseDTO;
import com.gasen.usercenterbackend.model.dto.ContentTrendResponseDTO;

public interface IDashboardService {
    DashboardStatsDTO getDashboardStats();
    
    UserTrendResponseDTO getUserTrend();
    
    ContentTrendResponseDTO getContentTrend();
} 