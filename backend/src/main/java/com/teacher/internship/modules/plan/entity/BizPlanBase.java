package com.teacher.internship.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("biz_plan_base")
@EqualsAndHashCode(callSuper = true)
public class BizPlanBase extends BaseEntity {

    private Long planId;
    private Long baseId;
    private Integer baseQuota;
    private Integer sortNo;
    private String status;
    private String remark;
}
