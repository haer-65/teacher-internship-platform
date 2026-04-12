package com.teacher.internship.config;

import com.teacher.internship.modules.score.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ArchivedPlanScoreBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ArchivedPlanScoreBackfillRunner.class);

    private final ScoreService scoreService;

    public ArchivedPlanScoreBackfillRunner(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Override
    public void run(ApplicationArguments args) {
        long generatedFinishedSheets = scoreService.backfillFinishedPlanDrafts();
        long generatedSheets = scoreService.backfillArchivedPlanDrafts();
        log.info("Plan score draft backfill finished, finishedGeneratedSheets={}, archivedGeneratedSheets={}",
                generatedFinishedSheets,
                generatedSheets);
    }
}
