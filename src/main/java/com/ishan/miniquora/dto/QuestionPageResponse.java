package com.ishan.miniquora.dto;

import java.util.List;

public record QuestionPageResponse(
        List<QuestionResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public QuestionPageResponse {
        items = List.copyOf(items);
    }
}
