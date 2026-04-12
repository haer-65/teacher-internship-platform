package com.teacher.internship.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class BaseInternshipBaseSaveRequest {

    @Size(max = 32, message = "实习基地编码长度不能超过32个字符")
    private String baseCode;

    @NotBlank(message = "实习基地名称不能为空")
    @Size(max = 128, message = "实习基地名称长度不能超过128个字符")
    private String baseName;

    @Size(max = 64, message = "省份长度不能超过64个字符")
    private String province;

    @Size(max = 64, message = "城市长度不能超过64个字符")
    private String city;

    @Size(max = 64, message = "区县长度不能超过64个字符")
    private String district;

    @Size(max = 255, message = "详细地址长度不能超过255个字符")
    private String address;

    @Size(max = 64, message = "联系人长度不能超过64个字符")
    private String contactPerson;

    @Size(max = 32, message = "联系电话长度不能超过32个字符")
    @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "联系电话格式不正确")
    private String contactPhone;
}
