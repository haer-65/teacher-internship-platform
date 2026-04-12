package com.teacher.internship.modules.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseGradeItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String gradeCode;
    private String gradeName;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
