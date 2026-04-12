package com.teacher.internship.modules.score.vo;

import lombok.Data;

@Data
public class ScoreRecalculateResultVO {

    private Long planId;
    private String planCode;
    private String planName;
    private long totalAssignments;
    private long generatedSheets;
}
