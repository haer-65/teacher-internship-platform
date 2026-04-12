package com.teacher.internship.modules.notice.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long noticeId;
    private String noticeType;
    private String noticeLevel;
    private String noticeTitle;
    private String noticeContent;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;
    private String senderName;
    private String targetRoleCode;
    private String relatedBusinessType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long relatedBusinessId;
    private String status;
    private LocalDateTime sendTime;
    private Integer readFlag;
    private LocalDateTime readTime;
}
