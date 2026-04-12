package com.teacher.internship.modules.application.vo;

import java.util.ArrayList;
import java.util.List;

public class AssignmentPageVO {

    private Long total;
    private Long page;
    private Long size;
    private List<AssignmentItemVO> records = new ArrayList<>();

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public List<AssignmentItemVO> getRecords() {
        return records;
    }

    public void setRecords(List<AssignmentItemVO> records) {
        this.records = records;
    }
}
