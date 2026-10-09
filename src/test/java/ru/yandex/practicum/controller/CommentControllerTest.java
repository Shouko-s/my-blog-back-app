package ru.yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.AbstractIntegrationTest;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    private long postId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String response = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Test title", "text", "Test text", "tags", List.of()))))
                .andReturn().getResponse().getContentAsString();
        postId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void create_shouldReturnCreatedComment() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("text", "Test comment", "postId", postId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.postId").value(postId));

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(jsonPath("$.commentsCount").value(1));
    }

    @Test
    void create_shouldReturn404_whenPostDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/comments", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("text", "Test comment", "postId", -1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAll_shouldReturnPostComments() throws Exception {
        createComment("Test comment 1");
        createComment("Test comment 2");

        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Test comment 1"))
                .andExpect(jsonPath("$[1].text").value("Test comment 2"));
    }

    @Test
    void findById_shouldReturnComment() throws Exception {
        long commentId = createComment("Test comment");

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    void update_shouldUsePostIdFromPath() throws Exception {
        long commentId = createComment("Test comment");

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("id", commentId, "text", "Updated test comment", "postId", -1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated test comment"))
                .andExpect(jsonPath("$.postId").value(postId));
    }

    @Test
    void delete_shouldRemoveComment() throws Exception {
        long commentId = createComment("Test comment");

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePost_shouldRemoveItsComments() throws Exception {
        createComment("Test comment");

        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                .andExpect(status().isNotFound());
    }

    private long createComment(String text) throws Exception {
        String response = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("text", text, "postId", postId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
