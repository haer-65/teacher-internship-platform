package com.teacher.internship.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notice")
public class NoticeProperties {

    private int deadlineReminderDays = 3;
    private String deadlineReminderCron = "0 0 8 * * ?";
    private boolean emailEnabled = false;
    private String emailFrom = "";

    public int getDeadlineReminderDays() {
        return deadlineReminderDays;
    }

    public void setDeadlineReminderDays(int deadlineReminderDays) {
        this.deadlineReminderDays = deadlineReminderDays;
    }

    public String getDeadlineReminderCron() {
        return deadlineReminderCron;
    }

    public void setDeadlineReminderCron(String deadlineReminderCron) {
        this.deadlineReminderCron = deadlineReminderCron;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }
}

