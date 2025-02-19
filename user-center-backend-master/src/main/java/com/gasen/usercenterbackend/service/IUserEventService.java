package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.UserEvent;

import java.util.List;

public interface IUserEventService {
    Boolean createUserEvent(Integer appId, Long eventId);

    IPage<UserEvent> pageByUserId(Page<UserEvent> userEventPage, QueryWrapper<UserEvent> queryWrapper);

    List<Long> getAllEventIdsByUserId(Integer userId);
}
