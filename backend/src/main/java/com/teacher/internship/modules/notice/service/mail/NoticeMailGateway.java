package com.teacher.internship.modules.notice.service.mail;

import com.teacher.internship.modules.notice.entity.BizNotice;
import com.teacher.internship.modules.system.entity.SysUser;

import java.util.List;

public interface NoticeMailGateway {

    void sendNoticeMail(BizNotice notice, List<SysUser> receivers);
}

