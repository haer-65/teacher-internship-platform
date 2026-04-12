package com.teacher.internship.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_param")
@EqualsAndHashCode(callSuper = true)
public class SysParam extends BaseEntity {

    private String paramCode;
    private String paramName;
    private String paramValue;
    private String paramType;
    private String status;
    private String remark;
}

