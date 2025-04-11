package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.mapper.*;
import com.gasen.usercenterbackend.model.dao.*;
import com.gasen.usercenterbackend.model.dto.*;
import com.gasen.usercenterbackend.service.IDashboardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private SubCommentMapper subCommentMapper;

    @Resource
    private ComplaintMapper complaintMapper;

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 用户统计
        stats.setUserCount(userMapper.selectCount(null));
        stats.setUserTrend(calculateUserTrend());
        
        // 帖子统计
        stats.setPostCount(postMapper.selectCount(null));
        stats.setPostTrend(calculatePostTrend());
        
        // 评论统计
        stats.setCommentCount(commentMapper.selectCount(null) + subCommentMapper.selectCount(null));
        stats.setCommentTrend(calculateCommentTrend());
        
        // 投诉统计
        LambdaQueryWrapper<Complaint> complaintWrapper = new LambdaQueryWrapper<>();
        complaintWrapper.eq(Complaint::getStatus, 0); // 待处理状态
        stats.setComplaintCount(complaintMapper.selectCount(null));
        stats.setComplaintTrend(calculateComplaintTrend());
        
        return stats;
    }

    @Override
    public UserTrendResponseDTO getUserTrend() {
        UserTrendResponseDTO response = new UserTrendResponseDTO();
        List<TrendDataDTO> trendData = new ArrayList<>();
        
        // 获取最近7天的数据
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(User::getCreateTime, date.format(DateTimeFormatter.ISO_DATE));
            
            TrendDataDTO data = new TrendDataDTO();
            data.setDate(date.format(DateTimeFormatter.ISO_DATE));
            data.setCount(userMapper.selectCount(wrapper));
            trendData.add(data);
        }
        
        response.setData(trendData);
        return response;
    }

    @Override
    public ContentTrendResponseDTO getContentTrend() {
        ContentTrendResponseDTO response = new ContentTrendResponseDTO();
        List<TrendDataDTO> postTrendData = new ArrayList<>();
        List<TrendDataDTO> commentTrendData = new ArrayList<>();
        
        // 获取最近7天的数据
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_DATE);
            
            // 帖子趋势
            LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
            postWrapper.like(Post::getCreateTime, dateStr);
            TrendDataDTO postData = new TrendDataDTO();
            postData.setDate(dateStr);
            postData.setCount(postMapper.selectCount(postWrapper));
            postTrendData.add(postData);
            
            // 评论趋势
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.like(Comment::getCreateTime, dateStr);
            LambdaQueryWrapper<SubComment> subCommentWrapper = new LambdaQueryWrapper<>();
            subCommentWrapper.like(SubComment::getCreateTime, dateStr);
            
            TrendDataDTO commentData = new TrendDataDTO();
            commentData.setDate(dateStr);
            commentData.setCount(commentMapper.selectCount(commentWrapper) + subCommentMapper.selectCount(subCommentWrapper));
            commentTrendData.add(commentData);
        }
        
        response.setPostData(postTrendData);
        response.setCommentData(commentTrendData);
        return response;
    }

    private Double calculateUserTrend() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        LambdaQueryWrapper<User> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.like(User::getCreateTime, today.format(DateTimeFormatter.ISO_DATE));
        Long todayCount = userMapper.selectCount(todayWrapper);
        
        LambdaQueryWrapper<User> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.like(User::getCreateTime, yesterday.format(DateTimeFormatter.ISO_DATE));
        Long yesterdayCount = userMapper.selectCount(yesterdayWrapper);
        
        return calculateTrend(todayCount, yesterdayCount);
    }

    private Double calculatePostTrend() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        LambdaQueryWrapper<Post> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.like(Post::getCreateTime, today.format(DateTimeFormatter.ISO_DATE));
        Long todayCount = postMapper.selectCount(todayWrapper);
        
        LambdaQueryWrapper<Post> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.like(Post::getCreateTime, yesterday.format(DateTimeFormatter.ISO_DATE));
        Long yesterdayCount = postMapper.selectCount(yesterdayWrapper);
        
        return calculateTrend(todayCount, yesterdayCount);
    }

    private Double calculateCommentTrend() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        LambdaQueryWrapper<Comment> todayCommentWrapper = new LambdaQueryWrapper<>();
        todayCommentWrapper.like(Comment::getCreateTime, today.format(DateTimeFormatter.ISO_DATE));
        LambdaQueryWrapper<SubComment> todaySubCommentWrapper = new LambdaQueryWrapper<>();
        todaySubCommentWrapper.like(SubComment::getCreateTime, today.format(DateTimeFormatter.ISO_DATE));
        Long todayCount = commentMapper.selectCount(todayCommentWrapper) + subCommentMapper.selectCount(todaySubCommentWrapper);
        
        LambdaQueryWrapper<Comment> yesterdayCommentWrapper = new LambdaQueryWrapper<>();
        yesterdayCommentWrapper.like(Comment::getCreateTime, yesterday.format(DateTimeFormatter.ISO_DATE));
        LambdaQueryWrapper<SubComment> yesterdaySubCommentWrapper = new LambdaQueryWrapper<>();
        yesterdaySubCommentWrapper.like(SubComment::getCreateTime, yesterday.format(DateTimeFormatter.ISO_DATE));
        Long yesterdayCount = commentMapper.selectCount(yesterdayCommentWrapper) + subCommentMapper.selectCount(yesterdaySubCommentWrapper);
        
        return calculateTrend(todayCount, yesterdayCount);
    }

    private Double calculateComplaintTrend() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        LambdaQueryWrapper<Complaint> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.like(Complaint::getCreateTime, today.format(DateTimeFormatter.ISO_DATE));
        Long todayCount = complaintMapper.selectCount(todayWrapper);
        
        LambdaQueryWrapper<Complaint> yesterdayWrapper = new LambdaQueryWrapper<>();
        yesterdayWrapper.like(Complaint::getCreateTime, yesterday.format(DateTimeFormatter.ISO_DATE));
        Long yesterdayCount = complaintMapper.selectCount(yesterdayWrapper);
        
        return calculateTrend(todayCount, yesterdayCount);
    }

    private Double calculateTrend(Long todayCount, Long yesterdayCount) {
        if (yesterdayCount == 0) {
            return todayCount == 0 ? 0.0 : 100.0;
        }
        return ((double) (todayCount - yesterdayCount) / yesterdayCount) * 100;
    }
} 