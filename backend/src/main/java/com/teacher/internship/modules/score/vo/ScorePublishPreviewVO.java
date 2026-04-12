package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScorePublishPreviewVO {

    private Long planId;
    private String planCode;
    private String planName;
    private String planStatus;
    private String scorePublishStatus;
    private Boolean canPublish;
    private String publishBlockedReason;
    private long total;
    private List<ScoreListItemVO> records = new ArrayList<>();
}
