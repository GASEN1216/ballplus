package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Long userCount;
    private Long postCount;
    private Long commentCount;
    private Long complaintCount;
    private Double userTrend;
    private Double postTrend;
    private Double commentTrend;
    private Double complaintTrend;
} 