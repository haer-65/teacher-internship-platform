package com.teacher.internship;

import com.teacher.internship.config.FileStorageProperties;
import com.teacher.internship.config.NoticeProperties;
import com.teacher.internship.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, FileStorageProperties.class, NoticeProperties.class})
public class InternshipPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipPlatformApplication.class, args);
    }
}
