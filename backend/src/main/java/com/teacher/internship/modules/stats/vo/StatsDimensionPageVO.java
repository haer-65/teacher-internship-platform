package com.teacher.internship.modules.stats.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StatsDimensionPageVO {

    private long total;
    private long page;
    private long size;
    private List<StatsDimensionItemVO> records = new ArrayList<>();
}
