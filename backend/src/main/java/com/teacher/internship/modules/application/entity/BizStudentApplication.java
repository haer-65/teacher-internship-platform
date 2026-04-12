package com.teacher.internship.modules.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_student_application")
@EqualsAndHashCode(callSuper = true)
public class BizStudentApplication extends BaseEntity {

    private Long planId;
    private Long studentId;
    private String intentionRegion;
    private String schoolType;
    private String personalStatement;
    private String applicationStatus;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedTime;
    private LocalDateTime submittedTime;
}

