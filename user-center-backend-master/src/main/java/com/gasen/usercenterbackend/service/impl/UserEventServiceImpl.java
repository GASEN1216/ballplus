package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.mapper.UserEventMapper;
import com.gasen.usercenterbackend.mapper.UserMapper;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dao.UserEvent;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.service.IUserEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserEventServiceImpl implements IUserEventService {

    @Resource
    private UserEventMapper userEventMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Boolean createUserEvent(Long appId, Long eventId) {
        return userEventMapper.insert(new UserEvent(appId, eventId)) > 0;
    }

    @Override
    public IPage<UserEvent> pageByUserId(Page<UserEvent> userEventPage, QueryWrapper<UserEvent> queryWrapper) {
        // 调用 MyBatis-Plus 提供的分页查询方法, eventPage 为分页参数, queryWrapper 为查询条件
        return userEventMapper.selectPage(userEventPage, queryWrapper);
    }

    @Override
    public List<Long> getAllEventIdsByUserId(Long userId) {
        return userEventMapper.selectList(new QueryWrapper<UserEvent>().eq("user_id", userId)).stream().map(UserEvent::getEventId).toList();
    }

    @Override
    public List<Long> getUserIdsByEventId(Long eventId) {
        return userEventMapper.selectList(new QueryWrapper<UserEvent>().eq("event_id", eventId)).stream().map(UserEvent::getUserId).toList();
    }

    @Override
    public List<User> getEventParticipants(Long eventId) {
        // 获取参与活动的所有用户ID
        List<Long> userIds = getUserIdsByEventId(eventId);
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 通过用户ID列表查询用户信息
        return userMapper.selectBatchIds(userIds);
    }

    @Override
    public boolean quitEvent(Long userId, Long eventId) {
        return userEventMapper.delete(new QueryWrapper<UserEvent>().eq("user_id", userId).eq("event_id", eventId)) > 0;
    }

    /**
     * 判断用户是否参与了活动
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否参与
     */
    @Override
    public boolean isUserParticipated(Long userId, Long eventId) {
        QueryWrapper<UserEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("event_id", eventId);
        
        return userEventMapper.selectOne(queryWrapper) != null;
    }

    @Override
    public CursorPageResponse<UserEvent> pageByUserIdWithCursor(Long userId, CursorPageRequest cursorRequest) {
        // 获取游标和分页参数
        String cursor = cursorRequest.getCursor();
        Integer pageSize = cursorRequest.getPageSize();
        Boolean isAsc = cursorRequest.getAsc();
        
        // 默认参数处理
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10; // 默认每页10条
        }
        
        // 默认降序
        boolean asc = Boolean.TRUE.equals(isAsc);
        
        // 构建查询条件
        LambdaQueryWrapper<UserEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEvent::getUserId, userId);
        
        // 添加游标条件
        if (StringUtils.hasText(cursor)) {
            try {
                // 假设游标是ISO格式的创建时间
                LocalDateTime cursorTime = LocalDateTime.parse(cursor, DateTimeFormatter.ISO_DATE_TIME);
                if (asc) {
                    // 升序查询，获取创建时间大于游标的记录
                    wrapper.gt(UserEvent::getCreateTime, cursorTime);
                    wrapper.orderByAsc(UserEvent::getCreateTime);
                } else {
                    // 降序查询，获取创建时间小于游标的记录
                    wrapper.lt(UserEvent::getCreateTime, cursorTime);
                    wrapper.orderByDesc(UserEvent::getCreateTime);
                }
            } catch (Exception e) {
                log.error("解析游标时间格式异常", e);
                // 游标解析失败，使用默认排序
                if (asc) {
                    wrapper.orderByAsc(UserEvent::getCreateTime);
                } else {
                    wrapper.orderByDesc(UserEvent::getCreateTime);
                }
            }
        } else {
            // 没有游标时使用默认排序
            if (asc) {
                wrapper.orderByAsc(UserEvent::getCreateTime);
            } else {
                wrapper.orderByDesc(UserEvent::getCreateTime);
            }
        }
        
        // 多查询一条用于判断是否有更多数据
        wrapper.last("LIMIT " + (pageSize + 1));
        
        // 执行查询
        List<UserEvent> records = userEventMapper.selectList(wrapper);
        
        // 判断是否有更多数据
        boolean hasMore = false;
        if (records.size() > pageSize) {
            hasMore = true;
            // 移除多查询的一条数据
            records.remove(records.size() - 1);
        }
        
        // 如果没有记录，直接返回空结果
        if (records.isEmpty()) {
            return new CursorPageResponse<>(new ArrayList<>(), null, false);
        }
        
        // 获取下一个游标
        String nextCursor = null;
        if (hasMore && !records.isEmpty()) {
            UserEvent lastRecord = records.get(records.size() - 1);
            nextCursor = lastRecord.getCreateTime().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        
        // 构建游标分页响应
        return new CursorPageResponse<>(records, nextCursor, hasMore);
    }
}
