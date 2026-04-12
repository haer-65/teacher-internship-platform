package com.teacher.internship.modules.notice.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NoticePageVO {

    private long total;
    private long page;
    private long size;
    private List<NoticeListItemVO> records = new ArrayList<>();
}

