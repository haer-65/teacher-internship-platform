package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScorePageVO {

    private long total;
    private long page;
    private long size;
    private List<ScoreListItemVO> records = new ArrayList<>();
}
