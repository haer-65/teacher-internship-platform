package com.teacher.internship.modules.material.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_material_version")
@EqualsAndHashCode(callSuper = true)
public class BizMaterialVersion extends BaseEntity {

    private Long materialId;
    private Integer versionNo;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileExt;
    private String mimeType;
    private String submitRemark;
    private Long submittedBy;
    private LocalDateTime submittedTime;
    private Integer isCurrent;
}

