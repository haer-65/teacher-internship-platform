package com.teacher.internship.modules.application.vo;

import java.util.ArrayList;
import java.util.List;

public class AssignmentImportResultVO {

    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private List<AssignmentImportErrorVO> errors = new ArrayList<>();

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getSuccessRows() {
        return successRows;
    }

    public void setSuccessRows(Integer successRows) {
        this.successRows = successRows;
    }

    public Integer getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(Integer failedRows) {
        this.failedRows = failedRows;
    }

    public List<AssignmentImportErrorVO> getErrors() {
        return errors;
    }

    public void setErrors(List<AssignmentImportErrorVO> errors) {
        this.errors = errors;
    }
}
