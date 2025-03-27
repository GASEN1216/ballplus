package com.gasen.usercenterbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gasen.usercenterbackend.model.dao.CreditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 信誉分记录表 Mapper 接口
 */
@Mapper
public interface CreditRecordMapper extends BaseMapper<CreditRecord> {
} 