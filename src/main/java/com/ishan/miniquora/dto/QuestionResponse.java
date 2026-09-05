package com.ishan.miniquora.dto;

import java.time.Instant;
import java.util.List;

public record QuestionResponse(
        String id,
        String title,
        String body,
        String authorId,
        List<String> tags,
        int voteCount,
        Instant createdAt,
        Instant updatedAt) {}
