package com.teacher.internship.modules.stats.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StatsDashboardVO {

    private StatsOverviewVO overview;
    private StatsScoreSummaryVO scoreSummary;
    private List<StatsScoreDistributionItemVO> scoreDistribution = new ArrayList<>();
}
