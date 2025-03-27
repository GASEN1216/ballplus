package com.gasen.usercenterbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gasen.usercenterbackend.model.dao.EventCancelRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动取消记录Mapper
 */
@Mapper
public interface EventCancelRecordMapper extends BaseMapper<EventCancelRecord> {
} 