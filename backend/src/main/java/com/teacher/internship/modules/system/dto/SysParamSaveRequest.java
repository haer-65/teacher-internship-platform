package com.teacher.internship.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SysParamSaveRequest {

    @NotBlank(message = "参数编码不能为空")
    @Size(max = 64, message = "参数编码长度不能超过64位")
    private String paramCode;

    @NotBlank(message = "参数名称不能为空")
    @Size(max = 128, message = "参数名称长度不能超过128位")
    private String paramName;

    @NotBlank(message = "参数值不能为空")
    @Size(max = 1000, message = "参数值长度不能超过1000位")
    private String paramValue;

    @NotBlank(message = "参数类型不能为空")
    @Size(max = 32, message = "参数类型长度不能超过32位")
    private String paramType;

    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;
}
