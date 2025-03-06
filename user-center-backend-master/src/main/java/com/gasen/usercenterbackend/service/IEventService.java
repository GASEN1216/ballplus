package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.Event;
import com.gasen.usercenterbackend.model.User;

import java.util.List;

public interface IEventService {
    Long createEvent(Event event);

    List<Event> listByIds(List<Long> eventIdLists);

    List<Event> findTemplates(Integer userId);


    Event getNearestEvent(List<Long> eventIds);

    Boolean deleteTemplateByPer(Integer userId, Integer templateId);

    Event getById(Long eventId);

    boolean cancelEvent(Integer userId, Long eventId);

    boolean reduceParticipants(Long eventId);

    boolean addParticipants(Long eventId);

    int scheduleUpdateEventState();

    List<Event> getAllEvents();

    Long matchEvent(User user, Float latitude, Float longitude);
}
