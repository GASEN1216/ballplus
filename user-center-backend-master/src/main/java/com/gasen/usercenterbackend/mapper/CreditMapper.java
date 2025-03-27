package com.gasen.usercenterbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gasen.usercenterbackend.model.entity.CreditRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 信誉分数据访问接口
 */
@Mapper
public interface CreditMapper extends BaseMapper<CreditRecord> {
} 