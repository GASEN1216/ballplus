package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.usercenterbackend.mapper.EventCancelRecordMapper;
import com.gasen.usercenterbackend.model.dao.EventCancelRecord;
import com.gasen.usercenterbackend.service.IEventCancelRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 活动取消记录服务实现类
 */
@Service
public class EventCancelRecordServiceImpl extends ServiceImpl<EventCancelRecordMapper, EventCancelRecord> implements IEventCancelRecordService {

    @Override
    public boolean saveEventCancelRecord(Long eventId, String cancelReason) {
        EventCancelRecord record = new EventCancelRecord();
        record.setEventId(eventId);
        record.setCancelReason(cancelReason);
        record.setCreateTime(LocalDateTime.now());
        return this.save(record);
    }
} 