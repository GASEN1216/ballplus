package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.mapper.UserEventMapper;
import com.gasen.usercenterbackend.model.UserEvent;
import com.gasen.usercenterbackend.service.IUserEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEventServiceImpl implements IUserEventService {

    @Resource
    private UserEventMapper userEventMapper;

    @Override
    public Boolean createUserEvent(Integer appId, Long eventId) {
        return userEventMapper.insert(new UserEvent(appId, eventId)) > 0;
    }

    @Override
    public IPage<UserEvent> pageByUserId(Page<UserEvent> userEventPage, QueryWrapper<UserEvent> queryWrapper) {
        // 调用 MyBatis-Plus 提供的分页查询方法, eventPage 为分页参数, queryWrapper 为查询条件
        return userEventMapper.selectPage(userEventPage, queryWrapper);
    }
}
