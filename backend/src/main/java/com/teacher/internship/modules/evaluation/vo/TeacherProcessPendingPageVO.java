package com.teacher.internship.modules.evaluation.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherProcessPendingPageVO {

    private Long page;
    private Long size;
    private Long total;
    private List<TeacherProcessPendingItemVO> records = new ArrayList<>();
}
