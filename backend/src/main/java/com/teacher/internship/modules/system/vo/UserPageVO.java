package com.teacher.internship.modules.system.vo;

import java.util.ArrayList;
import java.util.List;

public class UserPageVO {

    private Long total;
    private Long page;
    private Long size;
    private List<UserItemVO> records = new ArrayList<>();

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

    public List<UserItemVO> getRecords() {
        return records;
    }

    public void setRecords(List<UserItemVO> records) {
        this.records = records;
    }
}

