package com.teacher.internship.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_major")
@EqualsAndHashCode(callSuper = true)
public class BaseMajor extends BaseEntity {

    private Long deptId;
    private String majorCode;
    private String majorName;
    private String status;
}

