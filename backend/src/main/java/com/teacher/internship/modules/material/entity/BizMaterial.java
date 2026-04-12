package com.teacher.internship.modules.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_material")
@EqualsAndHashCode(callSuper = true)
public class BizMaterial extends BaseEntity {

    private Long assignmentId;
    private Long materialTypeId;
    private Long studentId;
    private Integer latestVersionNo;
    private String materialStatus;
    private LocalDateTime lastSubmitTime;
}

