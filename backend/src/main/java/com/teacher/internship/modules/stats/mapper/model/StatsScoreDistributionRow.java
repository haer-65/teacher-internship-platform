package com.teacher.internship.modules.stats.mapper.model;

import lombok.Data;

@Data
public class StatsScoreDistributionRow {

    private String bucketKey;
    private String bucketLabel;
    private Long scoreCount;
}
