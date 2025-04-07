package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.Event;
import com.gasen.usercenterbackend.model.dao.User;

import java.util.List;

public interface IEventService {
    Long createEvent(Event event);

    List<Event> listByIds(List<Long> eventIdLists);

    List<Event> findTemplates(Long userId);


    Event getNearestEvent(List<Long> eventIds);

    Boolean deleteTemplateByPer(Long userId, Long templateId);

    Event getById(Long eventId);

    boolean cancelEvent(Long userId, Long eventId);

    boolean reduceParticipants(Long eventId);

    boolean addParticipants(Long eventId);

    int scheduleUpdateEventState();

    List<Event> getAllEvents();

    Long matchEvent(User user, Float latitude, Float longitude);

    Boolean sendEventStartNotification(Long eventId);

    Boolean sendEventInOneHourStartNotification(Event event);

    /**
     * 判断活动是否已完成
     * @param eventId 活动ID
     * @return 是否已完成
     */
    boolean isEventCompleted(Long eventId);
}
