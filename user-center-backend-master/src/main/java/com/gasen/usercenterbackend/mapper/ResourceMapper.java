package com.gasen.usercenterbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gasen.usercenterbackend.model.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源Mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
} 