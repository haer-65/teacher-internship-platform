package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreDetailSnapshotVO {

    private String formulaVersion;
    private BigDecimal innerTeacherWeight;
    private BigDecimal baseTeacherWeight;
    private BigDecimal innerTeacherFinalScore;
    private BigDecimal baseTeacherFinalScore;
    private BigDecimal processScore;
    private BigDecimal finalScore;
    private BigDecimal autoTotalScore;
    private BigDecimal totalScore;
    private LocalDateTime calculatedTime;
    private List<ScoreMaterialDetailVO> materialDetails = new ArrayList<>();
}
