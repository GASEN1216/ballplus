package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.usercenterbackend.mapper.ScoreHistoryMapper;
import com.gasen.usercenterbackend.mapper.UserMapper;
import com.gasen.usercenterbackend.model.dao.ScoreHistory;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dto.ScoreHistoryDTO;
import com.gasen.usercenterbackend.service.IScoreService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreHistoryMapper, ScoreHistory> implements IScoreService {

    @Resource
    private UserMapper userMapper;
    
    @Resource
    private ScoreHistoryMapper scoreHistoryMapper;

    /**
     * 赛点变动类型常量
     */
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_ACTIVITY_PARTICIPATION = "ACTIVITY_PARTICIPATION";
    public static final String TYPE_ACTIVITY_CREATION = "ACTIVITY_CREATION";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordScoreChange(Long userId, Integer changeAmount, String type, String description) {
        // 1. 更新用户赛点
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        int newScore = user.getScore() + changeAmount;
        user.setScore(newScore);
        userMapper.updateById(user);
        
        // 2. 记录赛点变动
        ScoreHistory history = new ScoreHistory()
                .setUserId(userId)
                .setChangeAmount(changeAmount)
                .setType(type)
                .setDescription(description)
                .setCreatedTime(LocalDateTime.now());
        
        return save(history);
    }

    @Override
    public List<ScoreHistoryDTO> getUserScoreHistory(Long userId) {
        LambdaQueryWrapper<ScoreHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreHistory::getUserId, userId)
                .orderByDesc(ScoreHistory::getCreatedTime);
        
        List<ScoreHistory> historyList = list(queryWrapper);
        
        return historyList.stream().map(history -> {
            ScoreHistoryDTO dto = new ScoreHistoryDTO()
                    .setId(history.getId())
                    .setChangeAmount(history.getChangeAmount())
                    .setType(history.getType())
                    .setDescription(history.getDescription())
                    .setCreatedTime(history.getCreatedTime());
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processLoginScore(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查今日是否已经获得登录积分
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusSeconds(1);
        
        LambdaQueryWrapper<ScoreHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreHistory::getUserId, user.getId())
                .eq(ScoreHistory::getType, TYPE_LOGIN)
                .between(ScoreHistory::getCreatedTime, startOfDay, endOfDay);
        
        long count = count(queryWrapper);
        if (count > 0) {
            // 今日已经获得登录积分
            return true;
        }
        
        // 记录登录积分
        String description = "每日首次登录奖励";
        return recordScoreChange(Long.valueOf(user.getId()), 1, TYPE_LOGIN, description);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processActivityCompletionScore(Long creatorId, List<Long> participantIds, String activityName) {
        if (activityName == null) {
            activityName = "未命名活动";
        }
        
        // 活动参与者加分
        boolean result = true;
        for (Long userId : participantIds) {
            String description = "参与活动: " + activityName;
            result = result && recordScoreChange(userId, 1, TYPE_ACTIVITY_PARTICIPATION, description);
        }
        
        // 创建者额外加分
        if (creatorId != null && participantIds.contains(creatorId)) {
            String description = "创建活动: " + activityName;
            result = result && recordScoreChange(creatorId, 1, TYPE_ACTIVITY_CREATION, description);
        }
        
        return result;
    }
} 