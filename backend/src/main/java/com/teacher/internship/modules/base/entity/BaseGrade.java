package com.teacher.internship.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_grade")
@EqualsAndHashCode(callSuper = true)
public class BaseGrade extends BaseEntity {

    private String gradeCode;
    private String gradeName;
    private String status;
}

