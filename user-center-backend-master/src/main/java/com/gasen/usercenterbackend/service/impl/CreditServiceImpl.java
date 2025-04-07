package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.usercenterbackend.mapper.CreditMapper;
import com.gasen.usercenterbackend.mapper.UserMapper;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dto.CreditHistoryDTO;
import com.gasen.usercenterbackend.model.dto.CreditInfoDTO;
import com.gasen.usercenterbackend.model.entity.CreditRecord;
import com.gasen.usercenterbackend.service.CreditService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 信誉分服务实现类
 */
@Service
public class CreditServiceImpl extends ServiceImpl<CreditMapper, CreditRecord> implements CreditService {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private CreditMapper creditMapper;
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // 变动类型映射表
    private static final String[] CHANGE_TYPE_TITLES = {
        "未知变动",           // 0 (实际不应该有这个值)
        "投诉扣分",           // 1
        "取消活动扣分",        // 2
        "退出活动扣分",        // 3
        "管理员调整",         // 4
        "其他原因"            // 5
    };
    
    @Override
    public CreditInfoDTO getCreditInfo(Long userId) {
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        
        // 构建信誉分信息DTO
        CreditInfoDTO creditInfoDTO = new CreditInfoDTO();
        creditInfoDTO.setUserId(userId);
        creditInfoDTO.setCredit(user.getCredit());
        
        return creditInfoDTO;
    }
    
    @Override
    public List<CreditHistoryDTO> getCreditHistory(Long userId) {
        // 查询用户的信誉分变动记录
        LambdaQueryWrapper<CreditRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditRecord::getUserId, userId.intValue())
                .orderByDesc(CreditRecord::getCreateTime);
        List<CreditRecord> records = creditMapper.selectList(queryWrapper);
        
        // 转换为DTO列表
        List<CreditHistoryDTO> result = new ArrayList<>();
        for (CreditRecord record : records) {
            CreditHistoryDTO dto = new CreditHistoryDTO();
            dto.setId(record.getId());
            
            // 根据changeType获取对应的标题
            int typeIndex = record.getChangeType();
            if (typeIndex >= 0 && typeIndex < CHANGE_TYPE_TITLES.length) {
                dto.setTitle(CHANGE_TYPE_TITLES[typeIndex]);
            } else {
                dto.setTitle("信誉分变动");
            }
            
            dto.setReason(record.getChangeReason());
            dto.setChange(record.getCreditChange());
            
            // 格式化时间
            if (record.getCreateTime() != null) {
                dto.setCreateTime(dateFormat.format(record.getCreateTime()));
            }
            
            result.add(dto);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseCredit(Long userId, Integer points, String reason, Integer changeType) {
        if (points <= 0) {
            throw new IllegalArgumentException("增加的信誉分必须为正数");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 更新用户信誉分
        int newCredit = Math.min(user.getCredit() + points, 100); // 信誉分上限为100
        user.setCredit(newCredit);
        userMapper.updateById(user);
        
        // 添加信誉分变动记录
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeType(changeType);
        record.setChangeReason(reason);
        record.setCreditChange(points);
        record.setCreateTime(new Date());
        
        return creditMapper.insert(record) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseCredit(Long userId, Integer points, String reason, Integer changeType) {
        if (points <= 0) {
            throw new IllegalArgumentException("减少的信誉分必须为正数");
        }
        
        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 更新用户信誉分
        int newCredit = Math.max(user.getCredit() - points, 0); // 信誉分下限为0
        user.setCredit(newCredit);
        userMapper.updateById(user);
        
        // 添加信誉分变动记录
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeType(changeType);
        record.setChangeReason(reason);
        record.setCreditChange(-points);  // 减少信誉分记录为负数
        record.setCreateTime(new Date());
        
        return creditMapper.insert(record) > 0;
    }
    
    @Override
    public boolean addCreditRecord(CreditRecord creditRecord) {
        if (creditRecord == null || creditRecord.getUserId() == null) {
            return false;
        }
        
        // 确保创建时间不为空
        if (creditRecord.getCreateTime() == null) {
            creditRecord.setCreateTime(new Date());
        }
        
        return creditMapper.insert(creditRecord) > 0;
    }
} 