package com.teacher.internship.modules.stats.mapper;

import com.teacher.internship.modules.stats.mapper.model.StatsDimensionRow;
import com.teacher.internship.modules.stats.mapper.model.StatsOverviewRow;
import com.teacher.internship.modules.stats.mapper.model.StatsQueryParam;
import com.teacher.internship.modules.stats.mapper.model.StatsScoreDistributionRow;
import com.teacher.internship.modules.stats.mapper.provider.StatsSqlProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface StatsQueryMapper {

    @SelectProvider(type = StatsSqlProvider.class, method = "selectOverview")
    StatsOverviewRow selectOverview(@Param("p") StatsQueryParam param);

    @SelectProvider(type = StatsSqlProvider.class, method = "selectScoreDistribution")
    List<StatsScoreDistributionRow> selectScoreDistribution(@Param("p") StatsQueryParam param);

    @SelectProvider(type = StatsSqlProvider.class, method = "selectDimensionStats")
    List<StatsDimensionRow> selectDimensionStats(@Param("p") StatsQueryParam param);

    @SelectProvider(type = StatsSqlProvider.class, method = "countDimensionGroups")
    Long countDimensionGroups(@Param("p") StatsQueryParam param);
}
