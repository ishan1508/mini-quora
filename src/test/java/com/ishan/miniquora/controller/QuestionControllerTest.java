package com.ishan.miniquora.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndRetrievesQuestion() throws Exception {
        String questionId = createQuestion("""
                {
                  "title": "How does optimistic locking work?",
                  "body": "I want to prevent lost updates.",
                  "authorId": "user-123",
                  "tags": [" Java ", "concurrency", "java"]
                }
                """);

        mockMvc.perform(get("/questions/{questionId}", questionId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(questionId))
                .andExpect(jsonPath("$.title").value("How does optimistic locking work?"))
                .andExpect(jsonPath("$.body").value("I want to prevent lost updates."))
                .andExpect(jsonPath("$.authorId").value("user-123"))
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0]").value("java"))
                .andExpect(jsonPath("$.tags[1]").value("concurrency"))
                .andExpect(jsonPath("$.voteCount").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    @Test
    void supportsOptionalAuthorAndTags() throws Exception {
        String questionId = createQuestion("""
                {
                  "title": "Question without metadata",
                  "body": "Author and tags are optional."
                }
                """);

        mockMvc.perform(get("/questions/{questionId}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorId").value(nullValue()))
                .andExpect(jsonPath("$.tags").isEmpty());
    }

    @Test
    void listsQuestionsAndSearchesIgnoringCase() throws Exception {
        String uniqueTerm = "SearchMarkerAlpha";
        String questionId = createQuestion("""
                {
                  "title": "A searchable %s question",
                  "body": "This body is ordinary.",
                  "tags": ["tag-only-marker"]
                }
                """.formatted(uniqueTerm));

        mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.items[0]").exists());

        mockMvc.perform(get("/questions").queryParam("query", uniqueTerm.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(questionId));

        mockMvc.perform(get("/questions").queryParam("query", "tag-only-marker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void filtersByAllRequestedTagsIgnoringCase() throws Exception {
        String uniqueTerm = "TagFilterMarkerGamma";
        String matchingQuestionId = createQuestion("""
                {
                  "title": "%s complete match",
                  "body": "Contains both requested tags.",
                  "tags": ["Java-Filter", "Concurrency-Filter"]
                }
                """.formatted(uniqueTerm));
        createQuestion("""
                {
                  "title": "%s partial match",
                  "body": "Contains only one requested tag.",
                  "tags": ["java-filter"]
                }
                """.formatted(uniqueTerm));

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("tags", " JAVA-FILTER , concurrency-filter "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(matchingQuestionId));
    }

    @Test
    void sortsBySupportedFieldsAndDirection() throws Exception {
        String uniqueTerm = "SortMarkerDelta";
        String bravoQuestionId = createQuestion("""
                {
                  "title": "Bravo sorting question",
                  "body": "%s"
                }
                """.formatted(uniqueTerm));
        String alphaQuestionId = createQuestion("""
                {
                  "title": "Alpha sorting question",
                  "body": "%s"
                }
                """.formatted(uniqueTerm));
        String charlieQuestionId = createQuestion("""
                {
                  "title": "Charlie sorting question",
                  "body": "%s"
                }
                """.formatted(uniqueTerm));

        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", alphaQuestionId, "sort-user-1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", charlieQuestionId, "sort-user-1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", charlieQuestionId, "sort-user-2"))
                .andExpect(status().isOk());

        for (String sortBy : new String[] {"createdAt", "updatedAt", "voteCount", "title"}) {
            for (String direction : new String[] {"asc", "desc"}) {
                mockMvc.perform(get("/questions")
                                .queryParam("query", uniqueTerm)
                                .queryParam("sortBy", sortBy)
                                .queryParam("direction", direction))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.items.length()").value(3));
            }
        }

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("sortBy", "title")
                        .queryParam("direction", "asc"))
                .andExpect(jsonPath("$.items[0].id").value(alphaQuestionId));

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("sortBy", "title")
                        .queryParam("direction", "desc"))
                .andExpect(jsonPath("$.items[0].id").value(charlieQuestionId));

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("sortBy", "voteCount")
                        .queryParam("direction", "asc"))
                .andExpect(jsonPath("$.items[0].id").value(bravoQuestionId));

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("sortBy", "voteCount")
                        .queryParam("direction", "desc"))
                .andExpect(jsonPath("$.items[0].id").value(charlieQuestionId));
    }

    @Test
    void paginatesQuestionsWithMetadata() throws Exception {
        String uniqueTerm = "PaginationMarkerBeta";
        for (int index = 1; index <= 3; index++) {
            createQuestion("""
                    {
                      "title": "%s question %d",
                      "body": "Question used to verify pagination."
                    }
                    """.formatted(uniqueTerm, index));
        }

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("page", "0")
                        .queryParam("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/questions")
                        .queryParam("query", uniqueTerm)
                        .queryParam("page", "1")
                        .queryParam("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void rejectsInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get("/questions").queryParam("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"));

        mockMvc.perform(get("/questions").queryParam("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void rejectsInvalidFilterAndSortParameters() throws Exception {
        mockMvc.perform(get("/questions").queryParam("tags", "java,,concurrency"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"));

        mockMvc.perform(get("/questions").queryParam("sortBy", "unsupported"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));

        mockMvc.perform(get("/questions").queryParam("direction", "sideways"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void rejectsInvalidQuestion() throws Exception {
        mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "body": "",
                                  "authorId": " ",
                                  "tags": ["valid", " "]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void returnsNotFoundForUnknownQuestion() throws Exception {
        mockMvc.perform(get("/questions/{questionId}", "missing-question"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", "missing-question", "user-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void upvoteIsIdempotentPerUser() throws Exception {
        String questionId = createQuestion("""
                {
                  "title": "Can this be upvoted?",
                  "body": "Each user should only count once."
                }
                """);

        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", questionId, "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(1));

        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", questionId, "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(1));

        mockMvc.perform(put("/questions/{questionId}/votes/{userId}", questionId, "user-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteCount").value(2));
    }

    private String createQuestion(String requestBody) throws Exception {
        MvcResult result = mockMvc.perform(post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
