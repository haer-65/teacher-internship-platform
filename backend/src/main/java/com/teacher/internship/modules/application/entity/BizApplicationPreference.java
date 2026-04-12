package com.teacher.internship.modules.application.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("biz_application_preference")
@EqualsAndHashCode(callSuper = true)
public class BizApplicationPreference extends BaseEntity {

    private Long applicationId;
    private Long baseId;
    private Integer preferenceOrder;
}
