package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dao.UserEvent;

import java.util.List;

public interface IUserEventService {
    Boolean createUserEvent(Integer appId, Long eventId);

    IPage<UserEvent> pageByUserId(Page<UserEvent> userEventPage, QueryWrapper<UserEvent> queryWrapper);

    List<Long> getAllEventIdsByUserId(Integer userId);

    List<Integer> getUserIdsByEventId(Long eventId);
    
    List<User> getEventParticipants(Long eventId);

    boolean quitEvent(Integer userId, Long eventId);

    /**
     * 判断用户是否参与了活动
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否参与
     */
    boolean isUserParticipated(Integer userId, Long eventId);
}
