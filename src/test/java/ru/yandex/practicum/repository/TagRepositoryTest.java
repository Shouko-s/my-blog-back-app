package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.AbstractIntegrationTest;
import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void saveForPost_shouldReuseExistingTags() {
        Long firstPostId = postRepository.save(new Post(null, "Test post 1", "Test text", null)).id();
        Long secondPostId = postRepository.save(new Post(null, "Test post 2", "Test text", null)).id();

        tagRepository.saveForPost(firstPostId, List.of("test_tag_2", "test_tag_1"));
        tagRepository.saveForPost(secondPostId, List.of("test_tag_1"));

        assertEquals(Map.of(firstPostId, List.of("test_tag_1", "test_tag_2"), secondPostId, List.of("test_tag_1")),
                tagRepository.findNamesByPostIds(List.of(firstPostId, secondPostId)));
    }

    @Test
    void deleteForPost_shouldRemoveOnlyLinksOfThatPost() {
        Long firstPostId = postRepository.save(new Post(null, "Test post 1", "Test text", null)).id();
        Long secondPostId = postRepository.save(new Post(null, "Test post 2", "Test text", null)).id();
        tagRepository.saveForPost(firstPostId, List.of("test_tag_1"));
        tagRepository.saveForPost(secondPostId, List.of("test_tag_1"));

        tagRepository.deleteForPost(firstPostId);

        assertEquals(Map.of(secondPostId, List.of("test_tag_1")),
                tagRepository.findNamesByPostIds(List.of(firstPostId, secondPostId)));
    }

    @Test
    void findNamesByPostIds_shouldReturnEmptyMap_whenNoIds() {
        assertTrue(tagRepository.findNamesByPostIds(List.of()).isEmpty());
    }
}
