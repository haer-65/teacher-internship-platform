package com.teacher.internship.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BaseMajorUniqueNameBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BaseMajorUniqueNameBootstrapRunner.class);
    private static final String TABLE_NAME = "base_major";
    private static final String INDEX_NAME = "uk_base_major_name";

    private final JdbcTemplate jdbcTemplate;

    public BaseMajorUniqueNameBootstrapRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                TABLE_NAME,
                INDEX_NAME
        );
        if (exists != null && exists > 0) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE `base_major` ADD UNIQUE KEY `uk_base_major_name` (`major_name`, `deleted`)");
        log.info("Added unique index '{}' on table '{}'.", INDEX_NAME, TABLE_NAME);
    }
}
