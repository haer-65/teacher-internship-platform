package com.teacher.internship.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_notice_receiver")
@EqualsAndHashCode(callSuper = true)
public class BizNoticeReceiver extends BaseEntity {

    private Long noticeId;
    private Long receiverId;
    private Integer readFlag;
    private LocalDateTime readTime;
}

