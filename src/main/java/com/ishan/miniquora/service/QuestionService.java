package com.ishan.miniquora.service;

import com.ishan.miniquora.dto.CreateQuestionRequest;
import com.ishan.miniquora.dto.QuestionPageResponse;
import com.ishan.miniquora.dto.QuestionResponse;
import com.ishan.miniquora.exception.ResourceNotFoundException;
import com.ishan.miniquora.model.Question;
import com.ishan.miniquora.repository.QuestionRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private static final Comparator<Question> QUESTION_ORDER =
            Comparator.comparing((Question question) -> question.createdAt()).thenComparing(question -> question.id());

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public QuestionResponse create(CreateQuestionRequest request) {
        Instant now = Instant.now();
        Question question = new Question(
                UUID.randomUUID().toString(),
                request.title().trim(),
                request.body().trim(),
                normalizeAuthorId(request.authorId()),
                normalizeTags(request.tags()),
                Set.of(),
                now,
                now);
        return toResponse(questionRepository.save(question));
    }

    public QuestionResponse getById(String questionId) {
        return toResponse(findQuestion(questionId));
    }

    public QuestionPageResponse findAll(String query, int page, int size) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<QuestionResponse> matchingQuestions = questionRepository.findAll().stream()
                .filter(question -> matches(question, normalizedQuery))
                .sorted(QUESTION_ORDER)
                .map(QuestionService::toResponse)
                .toList();

        int totalElements = matchingQuestions.size();
        int totalPages = totalElements == 0 ? 0 : ((totalElements - 1) / size) + 1;
        long offset = (long) page * size;
        int fromIndex = offset >= totalElements ? totalElements : (int) offset;
        int toIndex = (int) Math.min(offset + size, totalElements);

        return new QuestionPageResponse(
                matchingQuestions.subList(fromIndex, toIndex),
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                totalPages == 0 || page >= totalPages - 1);
    }

    public QuestionResponse upvote(String questionId, String userId) {
        String normalizedUserId = userId.trim();
        Question question = questionRepository
                .addVote(questionId, normalizedUserId, Instant.now())
                .orElseThrow(() -> notFound(questionId));
        return toResponse(question);
    }

    private Question findQuestion(String questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> notFound(questionId));
    }

    private static boolean matches(Question question, String query) {
        if (query.isEmpty()) {
            return true;
        }

        return question.title().toLowerCase(Locale.ROOT).contains(query)
                || question.body().toLowerCase(Locale.ROOT).contains(query)
                || question.tags().stream()
                        .map(tag -> tag.toLowerCase(Locale.ROOT))
                        .anyMatch(tag -> tag.contains(query));
    }

    private static String normalizeAuthorId(String authorId) {
        return authorId == null ? null : authorId.trim();
    }

    private static List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }

        LinkedHashSet<String> normalizedTags = new LinkedHashSet<>();
        tags.stream()
                .map(tag -> tag.trim())
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .forEach(normalizedTags::add);
        return List.copyOf(normalizedTags);
    }

    private static QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.id(),
                question.title(),
                question.body(),
                question.authorId(),
                question.tags(),
                question.voteCount(),
                question.createdAt(),
                question.updatedAt());
    }

    private static ResourceNotFoundException notFound(String questionId) {
        return new ResourceNotFoundException("Question '" + questionId + "' was not found");
    }
}
