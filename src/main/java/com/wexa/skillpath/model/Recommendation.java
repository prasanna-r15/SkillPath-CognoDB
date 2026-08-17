package com.wexa.skillpath.model;

import java.util.List;

public record Recommendation(
        String skillId,
        String skillName,
        String priority,
        List<String> courses,
        List<String> prerequisitePath) {
}
