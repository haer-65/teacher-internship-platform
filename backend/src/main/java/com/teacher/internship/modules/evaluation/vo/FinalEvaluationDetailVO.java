package com.teacher.internship.modules.evaluation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FinalEvaluationDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignmentId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    private String planStatus;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long innerTeacherId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long baseTeacherId;

    private Boolean canEvaluate;

    private List<EvaluationRecordVO> records = new ArrayList<>();
}
