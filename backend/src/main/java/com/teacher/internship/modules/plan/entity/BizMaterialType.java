package com.teacher.internship.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_material_type")
@EqualsAndHashCode(callSuper = true)
public class BizMaterialType extends BaseEntity {

    private Long planId;
    private String typeCode;
    private String typeName;
    private Integer requiredFlag;
    private Integer maxSubmitCount;
    private LocalDateTime deadlineTime;
    private BigDecimal weight;
    private String status;
}

