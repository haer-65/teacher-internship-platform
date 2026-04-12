package com.teacher.internship.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_plan_attachment")
@EqualsAndHashCode(callSuper = true)
public class BizPlanAttachment extends BaseEntity {

    private Long planId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileExt;
    private String mimeType;
    private Long uploadedBy;
    private LocalDateTime uploadedTime;
}

