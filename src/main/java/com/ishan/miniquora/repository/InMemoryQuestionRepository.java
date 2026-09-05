package com.ishan.miniquora.repository;

import com.ishan.miniquora.model.Question;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryQuestionRepository implements QuestionRepository {

    private final ConcurrentMap<String, Question> questions = new ConcurrentHashMap<>();

    @Override
    public Question save(Question question) {
        questions.put(question.id(), question);
        return question;
    }

    @Override
    public Optional<Question> findById(String questionId) {
        return Optional.ofNullable(questions.get(questionId));
    }

    @Override
    public List<Question> findAll() {
        return List.copyOf(questions.values());
    }

    @Override
    public Optional<Question> addVote(String questionId, String userId, Instant votedAt) {
        Question updated = questions.computeIfPresent(
                questionId, (ignoredQuestionId, question) -> question.addVote(userId, votedAt));
        return Optional.ofNullable(updated);
    }
}
