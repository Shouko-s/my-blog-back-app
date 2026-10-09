package ru.yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.AbstractIntegrationTest;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostControllerTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void create_shouldReturnCreatedPost() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Test title", "text", "Test text", "tags", List.of("test_tag_1", "test_tag_2")))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test title"))
                .andExpect(jsonPath("$.text").value("Test text"))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("test_tag_1", "test_tag_2")))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void findById_shouldReturnFullText() throws Exception {
        String longText = "a".repeat(200);
        long postId = createPost("Test title", longText, List.of());

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.text").value(longText));
    }

    @Test
    void findById_shouldReturn404_whenPostDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", -1))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldChangePostAndTags() throws Exception {
        long postId = createPost("Test title", "Test text", List.of("test_tag"));

        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("id", postId, "title", "Updated test title", "text", "Updated test text", "tags", List.of("updated_test_tag")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated test title"))
                .andExpect(jsonPath("$.text").value("Updated test text"))
                .andExpect(jsonPath("$.tags", contains("updated_test_tag")));
    }

    @Test
    void delete_shouldRemovePost() throws Exception {
        long postId = createPost("Test title", "Test text", List.of());

        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isNotFound());
    }

    @Test
    void incrementLikes_shouldReturnNewLikesCount() throws Exception {
        long postId = createPost("Test title", "Test text", List.of());

        mockMvc.perform(post("/api/posts/{id}/likes", postId))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
        mockMvc.perform(post("/api/posts/{id}/likes", postId))
                .andExpect(content().string("2"));
    }

    @Test
    void image_shouldBeUploadedAndDownloaded() throws Exception {
        long postId = createPost("Test title", "Test text", List.of());
        byte[] bytes = {1, 2, 3, 4};

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", postId)
                        .file(new MockMultipartFile("image", "image.png", "image/png", bytes)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", postId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void findAll_shouldTruncateLongTextAndPaginate() throws Exception {
        createPost("Test post 1", "a".repeat(200), List.of());
        createPost("Test post 2", "Test text", List.of());
        createPost("Test post 3", "Test text", List.of());

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Test post 1"))
                .andExpect(jsonPath("$.posts[0].text").value("a".repeat(128) + "…"))
                .andExpect(jsonPath("$.hasPrev").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(2));
    }

    @Test
    void findAll_shouldFilterByTitleAndTags() throws Exception {
        createPost("Matching test post", "Test text", List.of("test_tag_1", "test_tag_2"));
        createPost("Matching test post with other tag", "Test text", List.of("test_tag_3"));
        createPost("Other test post", "Test text", List.of("test_tag_1", "test_tag_2"));

        mockMvc.perform(get("/api/posts")
                        .param("search", "  matching   #test_tag_1 #test_tag_2 ")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Matching test post"))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    private long createPost(String title, String text, List<String> tags) throws Exception {
        String response = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", title, "text", text, "tags", tags))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
