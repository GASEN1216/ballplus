package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dao.UserEvent;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;

import java.util.List;

public interface IUserEventService {
    Boolean createUserEvent(Long appId, Long eventId);

    /**
     * 根据用户ID分页查询用户参与的活动
     * 
     * @param userEventPage 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     * @deprecated 使用 pageByUserIdWithCursor 替代
     */
    @Deprecated
    IPage<UserEvent> pageByUserId(Page<UserEvent> userEventPage, QueryWrapper<UserEvent> queryWrapper);
    
    /**
     * 根据用户ID使用游标分页查询用户参与的活动
     * 
     * @param userId 用户ID
     * @param cursorRequest 游标分页请求
     * @return 游标分页结果
     */
    CursorPageResponse<UserEvent> pageByUserIdWithCursor(Long userId, CursorPageRequest cursorRequest);

    List<Long> getAllEventIdsByUserId(Long userId);

    List<Long> getUserIdsByEventId(Long eventId);
    
    List<User> getEventParticipants(Long eventId);

    boolean quitEvent(Long userId, Long eventId);

    /**
     * 判断用户是否参与了活动
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否参与
     */
    boolean isUserParticipated(Long userId, Long eventId);
}
