package com.teacher.internship.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class BaseGradeSaveRequest {

    @Size(max = 32, message = "年级编码长度不能超过32个字符")
    private String gradeCode;

    @NotBlank(message = "年级名称不能为空")
    @Size(max = 64, message = "年级名称长度不能超过64个字符")
    private String gradeName;
}
