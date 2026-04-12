package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScorePublishResultVO {

    private Long planId;
    private String planCode;
    private String planName;
    private long publishedCount;
    private LocalDateTime publishedTime;
}
