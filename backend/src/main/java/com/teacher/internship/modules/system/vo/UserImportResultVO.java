package com.teacher.internship.modules.system.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserImportResultVO {

    private int totalRows;
    private int successRows;
    private int failedRows;
    private List<UserImportErrorVO> errors = new ArrayList<>();
}

