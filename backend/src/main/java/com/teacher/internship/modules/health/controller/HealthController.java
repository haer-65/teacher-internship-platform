package com.teacher.internship.modules.health.controller;

import com.teacher.internship.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> check() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("time", LocalDateTime.now().format(FORMATTER));
        data.put("service", "internship-platform-backend");
        return ApiResponse.success(data);
    }
}
