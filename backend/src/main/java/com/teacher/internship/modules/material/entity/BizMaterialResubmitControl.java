package com.teacher.internship.modules.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_material_resubmit_control")
@EqualsAndHashCode(callSuper = true)
public class BizMaterialResubmitControl extends BaseEntity {

    private Long materialId;
    private Integer openFlag;
    private LocalDateTime openUntil;
    private String openReason;
    private Long openedBy;
    private LocalDateTime openedTime;
    private Long closedBy;
    private LocalDateTime closedTime;
}
