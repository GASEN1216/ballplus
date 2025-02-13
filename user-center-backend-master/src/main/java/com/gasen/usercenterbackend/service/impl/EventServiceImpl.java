package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.mapper.EventMapper;
import com.gasen.usercenterbackend.model.Event;
import com.gasen.usercenterbackend.service.IEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventServiceImpl implements IEventService {

    @Resource
    private EventMapper eventMapper;

    @Override
    public Long createEvent(Event event) {
        eventMapper.insert(event);
        return event.getId();
    }

    @Override
    public IPage<Event> page(Page<Event> eventPage) {
        // 调用 MyBatis-Plus 提供的分页查询方法, eventPage 为分页参数, null 为查询条件
        return eventMapper.selectPage(eventPage, null);
    }

    @Override
    public IPage<Event> pageByLocation(Page<Event> eventPage, QueryWrapper<Event> queryWrapper) {
        // 调用 MyBatis-Plus 提供的分页查询方法, eventPage 为分页参数, queryWrapper 为查询条件
        return eventMapper.selectPage(eventPage, queryWrapper);
    }
}
