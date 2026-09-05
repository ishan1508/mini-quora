package com.ishan.miniquora.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateQuestionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 10_000) String body,

        @Pattern(regexp = ".*\\S.*", message = "must not be blank") @Size(max = 100) String authorId,

        @Size(max = 10) List<@Valid @NotBlank @Size(max = 50) String> tags) {}
