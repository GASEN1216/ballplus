package com.gasen.usercenterbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gasen.usercenterbackend.model.dao.Complaint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {
} 