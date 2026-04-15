package com.teacher.internship.modules.plan.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.teacher.internship.modules.notice.service.NoticeTriggerService;
import com.teacher.internship.modules.plan.entity.BizInternshipPlan;
import com.teacher.internship.modules.plan.mapper.BizInternshipPlanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PlanAutoFinishScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlanAutoFinishScheduler.class);
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FINISHED = "FINISHED";

    private final BizInternshipPlanMapper planMapper;
    private final NoticeTriggerService noticeTriggerService;

    public PlanAutoFinishScheduler(BizInternshipPlanMapper planMapper,
                                   NoticeTriggerService noticeTriggerService) {
        this.planMapper = planMapper;
        this.noticeTriggerService = noticeTriggerService;
    }

    @Scheduled(cron = "${app.plan.auto-finish-cron:0 */1 * * * ?}")
    @Transactional(rollbackFor = Exception.class)
    public void scheduleAutoFinishPlans() {
        LocalDateTime now = LocalDateTime.now();
        List<BizInternshipPlan> plans = planMapper.selectList(new LambdaQueryWrapper<BizInternshipPlan>()
                .eq(BizInternshipPlan::getDeleted, 0L)
                .eq(BizInternshipPlan::getPlanStatus, STATUS_PUBLISHED)
                .isNotNull(BizInternshipPlan::getEndTime)
                .le(BizInternshipPlan::getEndTime, now));
        if (plans.isEmpty()) {
            return;
        }

        long finishedCount = 0L;
        for (BizInternshipPlan plan : plans) {
            if (plan == null || plan.getId() == null) {
                continue;
            }

            int updatedRows = planMapper.update(null, new LambdaUpdateWrapper<BizInternshipPlan>()
                    .eq(BizInternshipPlan::getId, plan.getId())
                    .eq(BizInternshipPlan::getDeleted, 0L)
                    .eq(BizInternshipPlan::getPlanStatus, STATUS_PUBLISHED)
                    .isNotNull(BizInternshipPlan::getEndTime)
                    .le(BizInternshipPlan::getEndTime, now)
                    .set(BizInternshipPlan::getPlanStatus, STATUS_FINISHED)
                    .set(BizInternshipPlan::getUpdatedBy, 0L)
                    .set(BizInternshipPlan::getUpdatedTime, now));
            if (updatedRows <= 0) {
                continue;
            }

            plan.setPlanStatus(STATUS_FINISHED);
            plan.setUpdatedBy(0L);
            plan.setUpdatedTime(now);
            noticeTriggerService.notifyPlanFinished(plan, null);
            finishedCount++;
        }

        if (finishedCount > 0) {
            LOGGER.info("Auto-finished {} plan(s) whose end time has been reached.", finishedCount);
        }
    }
}
