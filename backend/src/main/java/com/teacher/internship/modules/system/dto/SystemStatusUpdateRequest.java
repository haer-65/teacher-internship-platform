package com.teacher.internship.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SystemStatusUpdateRequest {

    @NotBlank(message = "状态不能为空")
    private String status;
}
