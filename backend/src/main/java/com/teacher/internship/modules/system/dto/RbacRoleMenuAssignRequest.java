package com.teacher.internship.modules.system.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RbacRoleMenuAssignRequest {

    private List<Long> menuIds = new ArrayList<>();
}

