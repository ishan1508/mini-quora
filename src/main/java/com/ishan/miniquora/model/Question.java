package com.ishan.miniquora.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Question(
        String id,
        String title,
        String body,
        String authorId,
        List<String> tags,
        Set<String> voterIds,
        Instant createdAt,
        Instant updatedAt) {

    public Question {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(body);
        tags = List.copyOf(tags);
        voterIds = Set.copyOf(voterIds);
        Objects.requireNonNull(createdAt);
        Objects.requireNonNull(updatedAt);
    }

    public int voteCount() {
        return voterIds.size();
    }

    public Question addVote(String userId, Instant votedAt) {
        if (voterIds.contains(userId)) {
            return this;
        }

        Set<String> updatedVoterIds = new HashSet<>(voterIds);
        updatedVoterIds.add(userId);
        return new Question(id, title, body, authorId, tags, updatedVoterIds, createdAt, votedAt);
    }
}
