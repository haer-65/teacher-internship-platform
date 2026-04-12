package com.teacher.internship.modules.stats.vo;

import lombok.Data;

@Data
public class StatsScoreDistributionItemVO {

    private String bucketKey;
    private String bucketLabel;
    private long scoreCount;
}
