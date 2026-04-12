package com.teacher.internship.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrapRunner.class);
    private static final String CORE_TABLE_NAME = "sys_user";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    @Value("${app.bootstrap.script-location-pattern:classpath:/db/bootstrap/V*__*.sql}")
    private String scriptLocationPattern;

    public DatabaseBootstrapRunner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isCoreTableReady()) {
            log.info("Database bootstrap skipped, table '{}' already exists.", CORE_TABLE_NAME);
            return;
        }

        List<Resource> scripts = resolveScripts();
        if (scripts.isEmpty()) {
            log.warn("Database bootstrap skipped, no scripts found for pattern: {}", scriptLocationPattern);
            return;
        }

        log.warn("Core table '{}' not found, starting bootstrap with {} script(s).", CORE_TABLE_NAME, scripts.size());
        executeScripts(scripts);
        log.info("Database bootstrap completed.");
    }

    private boolean isCoreTableReady() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                CORE_TABLE_NAME
        );
        return count != null && count > 0;
    }

    private List<Resource> resolveScripts() throws Exception {
        Resource[] resources = resourcePatternResolver.getResources(scriptLocationPattern);
        return Arrays.stream(resources)
                .filter(Resource::exists)
                .sorted(Comparator.comparing(resource -> {
                    String filename = resource.getFilename();
                    return StringUtils.hasText(filename) ? filename : "";
                }))
                .collect(Collectors.toList());
    }

    private void executeScripts(List<Resource> scripts) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                for (Resource script : scripts) {
                    log.info("Executing bootstrap script: {}", script.getFilename());
                    ScriptUtils.executeSqlScript(connection, new EncodedResource(script, StandardCharsets.UTF_8));
                }
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
}

