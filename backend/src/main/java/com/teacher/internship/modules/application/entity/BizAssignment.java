package com.teacher.internship.modules.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_assignment")
@EqualsAndHashCode(callSuper = true)
public class BizAssignment extends BaseEntity {

    private Long planId;
    private Long applicationId;
    private Long studentId;
    private Long baseId;
    private Long innerTeacherId;
    private Long baseTeacherId;
    private Integer versionNo;
    private Integer isCurrent;
    private String assignmentStatus;
    private String adjustReason;
    private Long assignedBy;
    private LocalDateTime assignedTime;
}

