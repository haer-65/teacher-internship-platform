package com.teacher.internship.modules.system.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageResultVO<T> {

    private long page;
    private long size;
    private long total;
    private List<T> records = new ArrayList<>();
}

