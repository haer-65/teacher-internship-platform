package com.teacher.internship.modules.notice.dto;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class NoticeSendRequest {

    private String noticeType;
    private String noticeLevel;

    @NotBlank(message = "通知标题不能为空")
    private String noticeTitle;

    @NotBlank(message = "通知内容不能为空")
    private String noticeContent;

    private List<Long> targetUserIds = new ArrayList<>();
    private String targetRoleCode;
    private String relatedBusinessType;
    private Long relatedBusinessId;
    private Boolean sendEmail;

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeLevel() {
        return noticeLevel;
    }

    public void setNoticeLevel(String noticeLevel) {
        this.noticeLevel = noticeLevel;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public List<Long> getTargetUserIds() {
        return targetUserIds;
    }

    public void setTargetUserIds(List<Long> targetUserIds) {
        this.targetUserIds = targetUserIds;
    }

    public String getTargetRoleCode() {
        return targetRoleCode;
    }

    public void setTargetRoleCode(String targetRoleCode) {
        this.targetRoleCode = targetRoleCode;
    }

    public String getRelatedBusinessType() {
        return relatedBusinessType;
    }

    public void setRelatedBusinessType(String relatedBusinessType) {
        this.relatedBusinessType = relatedBusinessType;
    }

    public Long getRelatedBusinessId() {
        return relatedBusinessId;
    }

    public void setRelatedBusinessId(Long relatedBusinessId) {
        this.relatedBusinessId = relatedBusinessId;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
}
