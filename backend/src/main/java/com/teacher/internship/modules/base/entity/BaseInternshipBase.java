package com.teacher.internship.modules.base.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("base_internship_base")
@EqualsAndHashCode(callSuper = true)
public class BaseInternshipBase extends BaseEntity {

    private String baseCode;
    private String baseName;
    private String province;
    private String city;
    private String district;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private String status;
}

