package com.teacher.internship.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("biz_notice")
@EqualsAndHashCode(callSuper = true)
public class BizNotice extends BaseEntity {

    private String noticeType;
    private String noticeLevel;
    private String noticeTitle;
    private String noticeContent;
    private Long senderId;
    private String targetRoleCode;
    private String relatedBusinessType;
    private Long relatedBusinessId;
    private LocalDateTime sendTime;
    private String status;
}

