package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.Event;

public interface IEventService {
    Long createEvent(Event event);

    IPage<Event> page(Page<Event> eventPage);

    IPage<Event> pageByLocation(Page<Event> eventPage, QueryWrapper<Event> queryWrapper);
}
