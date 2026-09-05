package com.ishan.miniquora.controller;

import com.ishan.miniquora.dto.CreateQuestionRequest;
import com.ishan.miniquora.dto.QuestionPageResponse;
import com.ishan.miniquora.dto.QuestionResponse;
import com.ishan.miniquora.service.QuestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(questionService.findAll(query, page, size));
    }

    @PutMapping("/{questionId}/votes/{userId}")
    public ResponseEntity<QuestionResponse> upvote(
            @PathVariable @NotBlank @Size(max = 100) String questionId,
            @PathVariable @NotBlank @Size(max = 100) String userId) {
        return ResponseEntity.ok(questionService.upvote(questionId, userId));
    }
}
