package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dto.CreditHistoryDTO;
import com.gasen.usercenterbackend.model.dto.CreditInfoDTO;
import com.gasen.usercenterbackend.model.entity.CreditRecord;

import java.util.List;

/**
 * 信誉分服务接口
 */
public interface CreditService {
    
    /**
     * 获取用户信誉分信息
     *
     * @param userId 用户ID
     * @return 信誉分信息
     */
    CreditInfoDTO getCreditInfo(Long userId);
    
    /**
     * 获取用户信誉分变动历史记录
     *
     * @param userId 用户ID
     * @return 信誉分变动记录列表
     */
    List<CreditHistoryDTO> getCreditHistory(Long userId);
    
    /**
     * 增加信誉分
     *
     * @param userId 用户ID
     * @param points 增加的分值
     * @param reason 详细原因
     * @param changeType 变动类型，参见CreditRecord.changeType枚举值
     * @return 是否成功
     */
    boolean increaseCredit(Long userId, Integer points, String reason, Integer changeType);
    
    /**
     * 减少信誉分
     *
     * @param userId 用户ID
     * @param points 减少的分值（正数）
     * @param reason 详细原因
     * @param changeType 变动类型，参见CreditRecord.changeType枚举值
     * @return 是否成功
     */
    boolean decreaseCredit(Long userId, Integer points, String reason, Integer changeType);
    
    /**
     * 添加信誉分变动记录
     *
     * @param creditRecord 信誉分变动记录
     * @return 是否成功
     */
    boolean addCreditRecord(CreditRecord creditRecord);
} 