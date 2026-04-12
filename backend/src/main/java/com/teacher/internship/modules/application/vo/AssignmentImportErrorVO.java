package com.teacher.internship.modules.application.vo;

public class AssignmentImportErrorVO {

    private Integer rowNumber;
    private String message;

    public AssignmentImportErrorVO() {
    }

    public AssignmentImportErrorVO(Integer rowNumber, String message) {
        this.rowNumber = rowNumber;
        this.message = message;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
