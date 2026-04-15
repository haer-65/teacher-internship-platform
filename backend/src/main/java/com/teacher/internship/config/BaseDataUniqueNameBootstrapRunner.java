package com.teacher.internship.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BaseDataUniqueNameBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BaseDataUniqueNameBootstrapRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public BaseDataUniqueNameBootstrapRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureUniqueIndex("base_department", "uk_base_department_name", "dept_name");
        ensureUniqueIndex("base_grade", "uk_base_grade_name", "grade_name");
        ensureUniqueIndex("base_internship_base", "uk_base_internship_base_name", "base_name");
    }

    private void ensureUniqueIndex(String tableName, String indexName, String columnName) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.statistics " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class,
                tableName,
                indexName
        );
        if (exists != null && exists > 0) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD UNIQUE KEY `" + indexName + "` (`" + columnName + "`, `deleted`)");
        log.info("Added unique index '{}' on table '{}'.", indexName, tableName);
    }
}
