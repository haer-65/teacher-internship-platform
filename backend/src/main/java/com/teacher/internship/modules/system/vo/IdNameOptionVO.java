package com.teacher.internship.modules.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class IdNameOptionVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
}
