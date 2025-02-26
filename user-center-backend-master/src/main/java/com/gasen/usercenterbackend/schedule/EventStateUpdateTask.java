package com.gasen.usercenterbackend.schedule;

import com.gasen.usercenterbackend.service.IEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@Slf4j
public class EventStateUpdateTask {

    @Resource
    private IEventService eventService;

    /**
     * 每天 15:30 运行，更新 event 表中 eventDate 早于今天的活动状态
     */
    @Scheduled(cron = "0 30 15 * * ?")
    public void updateEventState() {
        int count = eventService.scheduleUpdateEventState();
        if (count > 0) {
            log.info("更新了 {} 条活动状态", count);
        } else {
            log.info("今天没有活动需要更新");
        }
    }
}