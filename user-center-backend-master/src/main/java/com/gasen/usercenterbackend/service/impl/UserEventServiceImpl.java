package com.gasen.usercenterbackend.service.impl;

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
}
