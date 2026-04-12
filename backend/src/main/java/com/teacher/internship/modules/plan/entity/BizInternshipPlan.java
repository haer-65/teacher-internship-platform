package com.teacher.internship.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_internship_plan")
@EqualsAndHashCode(callSuper = true)
public class BizInternshipPlan extends BaseEntity {

    private String planCode;
    private String planName;
    private String academicYear;
    private String term;
    private Long deptId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime applyDeadline;
    private Integer studentQuota;
    private String description;
    private BigDecimal innerTeacherWeight;
    private BigDecimal baseTeacherWeight;
    private String planStatus;
    private String scorePublishStatus;
    private LocalDateTime publishedTime;
    private LocalDateTime archivedTime;
}

