package com.teacher.internship.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_department")
@EqualsAndHashCode(callSuper = true)
public class BaseDepartment extends BaseEntity {

    private String deptCode;
    private String deptName;
    private Long parentId;
    private String leaderName;
    private String contactPhone;
    private String status;
}

