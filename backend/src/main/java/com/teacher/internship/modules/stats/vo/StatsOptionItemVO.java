package com.teacher.internship.modules.stats.vo;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class StatsOptionItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
}
