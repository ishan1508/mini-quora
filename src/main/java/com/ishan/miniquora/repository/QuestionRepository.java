package com.ishan.miniquora.repository;

import com.ishan.miniquora.model.Question;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository {

    Question save(Question question);

    Optional<Question> findById(String questionId);

    List<Question> findAll();

    Optional<Question> addVote(String questionId, String userId, Instant votedAt);
}
