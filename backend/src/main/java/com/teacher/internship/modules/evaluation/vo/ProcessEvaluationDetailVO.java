package com.teacher.internship.modules.evaluation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessEvaluationDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialVersionId;
    private Integer materialVersionNo;
    private String fileName;
    private Long fileSize;
    private String fileExt;
    private String mimeType;
    private String previewUrl;
    private String downloadUrl;
    private String submitRemark;
    private LocalDateTime submittedTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignmentId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialTypeId;
    private String materialTypeCode;
    private String materialTypeName;
    private LocalDateTime materialDeadlineTime;

    private Boolean canEvaluate;

    private List<EvaluationRecordVO> records = new ArrayList<>();
}
