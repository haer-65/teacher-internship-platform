package com.teacher.internship.modules.stats.enums;

import java.util.Locale;

public enum StatsDimensionType {
    SUMMARY("SUMMARY", "Current Scope"),
    DEPARTMENT("DEPARTMENT", "Department"),
    MAJOR("MAJOR", "Major"),
    GRADE("GRADE", "Grade"),
    BASE("BASE", "Internship Base"),
    TEACHER("TEACHER", "Teacher"),
    PLAN("PLAN", "Plan");

    private final String code;
    private final String label;

    StatsDimensionType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static StatsDimensionType fromCode(String code) {
        if (code == null) {
            return DEPARTMENT;
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (StatsDimensionType item : values()) {
            if (item.code.equals(normalized)) {
                return item;
            }
        }
        return DEPARTMENT;
    }
}
