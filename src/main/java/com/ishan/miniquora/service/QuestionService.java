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
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

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

    public QuestionPageResponse findAll(
            String query, String tags, String sortBy, String direction, int page, int size) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        Set<String> normalizedTags = normalizeFilterTags(tags);
        Comparator<Question> questionOrder = questionOrder(sortBy, direction);
        List<QuestionResponse> matchingQuestions = questionRepository.findAll().stream()
                .filter(question -> matchesKeyword(question, normalizedQuery))
                .filter(question -> matchesTags(question, normalizedTags))
                .sorted(questionOrder)
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

    private static boolean matchesKeyword(Question question, String query) {
        if (query.isEmpty()) {
            return true;
        }

        return question.title().toLowerCase(Locale.ROOT).contains(query)
                || question.body().toLowerCase(Locale.ROOT).contains(query);
    }

    private static boolean matchesTags(Question question, Set<String> tags) {
        return tags.isEmpty() || question.tags().containsAll(tags);
    }

    private static Set<String> normalizeFilterTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Set.of();
        }

        return List.of(tags.split(",")).stream()
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Comparator<Question> questionOrder(String sortBy, String direction) {
        Comparator<Question> comparator =
                switch (sortBy) {
                    case "createdAt" -> Comparator.comparing((Question question) -> question.createdAt());
                    case "updatedAt" -> Comparator.comparing((Question question) -> question.updatedAt());
                    case "voteCount" -> Comparator.comparingInt(question -> question.voteCount());
                    case "title" ->
                        Comparator.comparing((Question question) -> question.title(), String.CASE_INSENSITIVE_ORDER);
                    default -> throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
                };

        if ("desc".equals(direction)) {
            comparator = comparator.reversed();
        }
        return comparator.thenComparing(question -> question.id());
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
