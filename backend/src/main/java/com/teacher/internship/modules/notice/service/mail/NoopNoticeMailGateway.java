package com.teacher.internship.modules.notice.service.mail;

import com.teacher.internship.config.NoticeProperties;
import com.teacher.internship.modules.notice.entity.BizNotice;
import com.teacher.internship.modules.system.entity.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NoopNoticeMailGateway implements NoticeMailGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoopNoticeMailGateway.class);

    private final NoticeProperties noticeProperties;

    public NoopNoticeMailGateway(NoticeProperties noticeProperties) {
        this.noticeProperties = noticeProperties;
    }

    @Override
    public void sendNoticeMail(BizNotice notice, List<SysUser> receivers) {
        if (!noticeProperties.isEmailEnabled() || notice == null || CollectionUtils.isEmpty(receivers)) {
            return;
        }
        String receiverSummary = receivers.stream()
                .map(SysUser::getEmail)
                .collect(Collectors.joining(","));
        LOGGER.info("Mail gateway placeholder invoked. noticeId={}, receivers={}, from={}",
                notice.getId(),
                receiverSummary,
                noticeProperties.getEmailFrom());
    }
}

