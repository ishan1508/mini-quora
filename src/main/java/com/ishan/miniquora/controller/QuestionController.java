package com.ishan.miniquora.controller;

import com.ishan.miniquora.dto.CreateQuestionRequest;
import com.ishan.miniquora.dto.QuestionPageResponse;
import com.ishan.miniquora.dto.QuestionResponse;
import com.ishan.miniquora.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/questions")
public class QuestionController {

    private static final String TAG_VALUE_PATTERN = "(?:[^,\\s]|[^,\\s][^,]{0,48}[^,\\s])";
    private static final String TAGS_PATTERN =
            "\\s*(?:" + TAG_VALUE_PATTERN + "(?:\\s*,\\s*" + TAG_VALUE_PATTERN + "){0,9})?\\s*";

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse question = questionService.create(request);
        return ResponseEntity.created(URI.create("/questions/" + question.id())).body(question);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getById(@PathVariable @NotBlank @Size(max = 100) String questionId) {
        return ResponseEntity.ok(questionService.getById(questionId));
    }

    @GetMapping
    public ResponseEntity<QuestionPageResponse> findAll(
            @RequestParam(required = false) @Size(max = 200) String query,
            @RequestParam(required = false)
                    @Pattern(regexp = TAGS_PATTERN, message = "must contain 1 to 10 comma-separated tags") String tags,
            @RequestParam(defaultValue = "createdAt")
                    @Pattern(
                            regexp = "createdAt|updatedAt|voteCount|title",
                            message = "must be one of createdAt, updatedAt, voteCount, or title")
                    String sortBy,
            @RequestParam(defaultValue = "desc") @Pattern(regexp = "asc|desc", message = "must be either asc or desc") String direction,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(questionService.findAll(query, tags, sortBy, direction, page, size));
    }

    @PutMapping("/{questionId}/votes/{userId}")
    public ResponseEntity<QuestionResponse> upvote(
            @PathVariable @NotBlank @Size(max = 100) String questionId,
            @PathVariable @NotBlank @Size(max = 100) String userId) {
        return ResponseEntity.ok(questionService.upvote(questionId, userId));
    }
}
