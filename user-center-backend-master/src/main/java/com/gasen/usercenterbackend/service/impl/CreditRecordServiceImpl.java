package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.usercenterbackend.mapper.CreditRecordMapper;
import com.gasen.usercenterbackend.model.dao.CreditRecord;
import com.gasen.usercenterbackend.service.ICreditRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 信誉分记录表 服务实现类
 */
@Service
@Slf4j
public class CreditRecordServiceImpl extends ServiceImpl<CreditRecordMapper, CreditRecord> implements ICreditRecordService {
    
    /**
     * 添加信誉分记录
     * @param userId 用户ID
     * @param creditChange 信誉分变动值
     * @param changeType 变动类型
     * @param changeReason 变动原因
     * @param relationId 关联ID
     * @return 是否成功
     */
    @Override
    public boolean addCreditRecord(Long userId, Integer creditChange, Integer changeType, String changeReason, Long relationId) {
        CreditRecord record = new CreditRecord()
                .setUserId(userId)
                .setCreditChange(creditChange)
                .setChangeType(changeType)
                .setChangeReason(changeReason)
                .setRelationId(relationId)
                .setCreateTime(LocalDateTime.now());
        
        boolean saved = save(record);
        if (saved) {
            log.info("用户 {} 信誉分变动 {}, 原因: {}", userId, creditChange, changeReason);
        } else {
            log.error("用户 {} 信誉分记录保存失败", userId);
        }
        
        return saved;
    }
    
    /**
     * 计算用户当前信誉分
     * @param userId 用户ID
     * @return 当前信誉分
     */
    @Override
    public int calculateUserCredit(Long userId) {
        // 默认初始信誉分为100
        int baseCredit = 100;
        
        // 查询用户所有信誉分记录
        QueryWrapper<CreditRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        // 计算总变动值
        int totalChange = list(queryWrapper).stream()
                .mapToInt(CreditRecord::getCreditChange)
                .sum();
        
        // 计算最终信誉分
        int finalCredit = baseCredit + totalChange;
        
        // 限制信誉分范围在0-100之间
        return Math.max(0, Math.min(100, finalCredit));
    }
} 