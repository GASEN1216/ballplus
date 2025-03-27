package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.usercenterbackend.model.dao.CreditRecord;

/**
 * 信誉分记录表 服务接口
 */
public interface ICreditRecordService extends IService<CreditRecord> {
    
    /**
     * 添加信誉分记录
     * @param userId 用户ID
     * @param creditChange 信誉分变动值
     * @param changeType 变动类型
     * @param changeReason 变动原因
     * @param relationId 关联ID
     * @return 是否成功
     */
    boolean addCreditRecord(Integer userId, Integer creditChange, Integer changeType, String changeReason, Long relationId);
    
    /**
     * 计算用户当前信誉分
     * @param userId 用户ID
     * @return 当前信誉分
     */
    int calculateUserCredit(Integer userId);
} 