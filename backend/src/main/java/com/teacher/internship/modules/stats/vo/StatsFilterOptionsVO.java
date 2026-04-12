package com.teacher.internship.modules.stats.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StatsFilterOptionsVO {

    private List<StatsOptionItemVO> departments = new ArrayList<>();
    private List<StatsOptionItemVO> majors = new ArrayList<>();
    private List<StatsOptionItemVO> grades = new ArrayList<>();
    private List<StatsOptionItemVO> bases = new ArrayList<>();
    private List<StatsOptionItemVO> teachers = new ArrayList<>();
    private List<StatsOptionItemVO> plans = new ArrayList<>();
}
