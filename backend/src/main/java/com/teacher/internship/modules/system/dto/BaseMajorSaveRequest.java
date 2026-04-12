package com.teacher.internship.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class BaseMajorSaveRequest {

    @NotNull(message = "院系标识不能为空")
    private Long deptId;

    @Size(max = 32, message = "专业编码长度不能超过32个字符")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 64, message = "专业名称长度不能超过64个字符")
    private String majorName;
}
