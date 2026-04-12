package com.teacher.internship.modules.notice.schedule;

import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NoticeDeadlineScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoticeDeadlineScheduler.class);

    private final NoticeTriggerService noticeTriggerService;

    public NoticeDeadlineScheduler(NoticeTriggerService noticeTriggerService) {
        this.noticeTriggerService = noticeTriggerService;
    }

    @Scheduled(cron = "${app.notice.deadline-reminder-cron:0 0 8 * * ?}")
    public void scheduleDeadlineReminder() {
        long generated = noticeTriggerService.triggerMaterialDeadlineReminder();
        if (generated > 0) {
            LOGGER.info("Material deadline reminder generated {} notice(s).", generated);
        }
    }
}

