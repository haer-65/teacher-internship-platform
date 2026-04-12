package com.teacher.internship.modules.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseDepartmentItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String deptCode;
    private String deptName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    private String leaderName;
    private String contactPhone;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
