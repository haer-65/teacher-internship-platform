package com.teacher.internship.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class BaseDepartmentSaveRequest {

    @Size(max = 32, message = "院系编码长度不能超过32个字符")
    private String deptCode;

    @NotBlank(message = "院系名称不能为空")
    @Size(max = 64, message = "院系名称长度不能超过64个字符")
    private String deptName;

    private Long parentId;

    @Size(max = 64, message = "负责人长度不能超过64个字符")
    private String leaderName;

    @Size(max = 32, message = "手机号长度不能超过32个字符")
    @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "手机号格式不正确")
    private String contactPhone;
}
