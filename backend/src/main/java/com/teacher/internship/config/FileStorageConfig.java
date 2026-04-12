package com.teacher.internship.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(FileStorageConfig.class);
    private final FileStorageProperties properties;

    public FileStorageConfig(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initRootDirectory() {
        Path root = Paths.get(properties.getRootPath());
        try {
            Files.createDirectories(root);
            log.info("File storage root initialized: {}", root.toAbsolutePath());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize file storage path: " + root, ex);
        }
    }
}
