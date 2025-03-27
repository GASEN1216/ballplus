package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.usercenterbackend.model.dao.EventCancelRecord;

/**
 * 活动取消记录服务接口
 */
public interface IEventCancelRecordService extends IService<EventCancelRecord> {
    
    /**
     * 保存活动取消记录
     * 
     * @param eventId 活动ID
     * @param cancelReason 取消原因
     * @return 是否保存成功
     */
    boolean saveEventCancelRecord(Long eventId, String cancelReason);
} 