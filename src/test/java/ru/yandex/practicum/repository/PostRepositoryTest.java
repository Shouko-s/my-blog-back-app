package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.AbstractIntegrationTest;
import ru.yandex.practicum.model.Post;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void save_shouldGenerateIdAndStartWithZeroLikes() {
        Post saved = postRepository.save(new Post(null, "Test title", "Test text", null));

        assertNotNull(saved.id());
        Post found = postRepository.findById(saved.id()).orElseThrow();
        assertEquals("Test title", found.title());
        assertEquals("Test text", found.text());
        assertEquals(0L, found.likesCount());
    }

    @Test
    void findById_shouldReturnEmpty_whenPostDoesNotExist() {
        assertTrue(postRepository.findById(-1L).isEmpty());
    }

    @Test
    void update_shouldChangeTitleAndText() {
        Post saved = postRepository.save(new Post(null, "Test title", "Test text", null));

        boolean updated = postRepository.update(new Post(saved.id(), "Updated test title", "Updated test text", null));

        assertTrue(updated);
        Post found = postRepository.findById(saved.id()).orElseThrow();
        assertEquals("Updated test title", found.title());
        assertEquals("Updated test text", found.text());
    }

    @Test
    void update_shouldReturnFalse_whenPostDoesNotExist() {
        assertFalse(postRepository.update(new Post(-1L, "Test title", "Test text", null)));
    }

    @Test
    void deleteById_shouldRemovePost() {
        Post saved = postRepository.save(new Post(null, "Test title", "Test text", null));

        assertTrue(postRepository.deleteById(saved.id()));
        assertFalse(postRepository.existsById(saved.id()));
        assertFalse(postRepository.deleteById(saved.id()));
    }

    @Test
    void incrementLikes_shouldIncreaseLikesCountByOne() {
        Post saved = postRepository.save(new Post(null, "Test title", "Test text", null));

        postRepository.incrementLikes(saved.id());
        postRepository.incrementLikes(saved.id());

        assertEquals(2L, postRepository.findLikesCountById(saved.id()).orElseThrow());
    }

    @Test
    void incrementLikes_shouldReturnFalse_whenPostDoesNotExist() {
        assertFalse(postRepository.incrementLikes(-1L));
    }

    @Test
    void findPage_shouldReturnAllPostsNewestFirst_whenNoFilters() {
        Post first = postRepository.save(new Post(null, "Test post 1", "Test text", null));
        Post second = postRepository.save(new Post(null, "Test post 2", "Test text", null));

        List<Post> posts = postRepository.findPage("", List.of(), 10, 0);

        assertEquals(List.of(second.id(), first.id()), posts.stream().map(Post::id).toList());
        assertEquals(2, postRepository.count("", List.of()));
    }

    @Test
    void findPage_shouldFilterByTitleSubstringIgnoringCase() {
        Post spring = postRepository.save(new Post(null, "Matching test post", "Test text", null));
        postRepository.save(new Post(null, "Other test post", "Test text", null));

        List<Post> posts = postRepository.findPage("matching", List.of(), 10, 0);

        assertEquals(List.of(spring.id()), posts.stream().map(Post::id).toList());
        assertEquals(1, postRepository.count("matching", List.of()));
    }

    @Test
    void findPage_shouldTreatLikeWildcardsLiterally() {
        Post percent = postRepository.save(new Post(null, "Test 100% done", "Test text", null));
        postRepository.save(new Post(null, "Test 100 done", "Test text", null));

        List<Post> posts = postRepository.findPage("%", List.of(), 10, 0);

        assertEquals(List.of(percent.id()), posts.stream().map(Post::id).toList());
    }

    @Test
    void findPage_shouldFilterByAllTags() {
        Post both = postRepository.save(new Post(null, "Test post with both tags", "Test text", null));
        Post onlyJava = postRepository.save(new Post(null, "Test post with one tag", "Test text", null));
        tagRepository.saveForPost(both.id(), List.of("test_tag_1", "test_tag_2"));
        tagRepository.saveForPost(onlyJava.id(), List.of("test_tag_1"));

        assertEquals(List.of(both.id()),
                postRepository.findPage("", List.of("test_tag_1", "test_tag_2"), 10, 0).stream().map(Post::id).toList());
        assertEquals(2, postRepository.count("", List.of("test_tag_1")));
    }

    @Test
    void findPage_shouldFilterByTitleAndTags() {
        Post match = postRepository.save(new Post(null, "Matching test post", "Test text", null));
        Post wrongTitle = postRepository.save(new Post(null, "Other test post", "Test text", null));
        postRepository.save(new Post(null, "Matching test post without tags", "Test text", null));
        tagRepository.saveForPost(match.id(), List.of("test_tag_1"));
        tagRepository.saveForPost(wrongTitle.id(), List.of("test_tag_1"));

        List<Post> posts = postRepository.findPage("matching", List.of("test_tag_1"), 10, 0);

        assertEquals(List.of(match.id()), posts.stream().map(Post::id).toList());
    }

    @Test
    void findPage_shouldApplyLimitAndOffset() {
        for (int i = 1; i <= 5; i++) {
            postRepository.save(new Post(null, "Test post " + i, "Test text", null));
        }

        List<Post> page = postRepository.findPage("", List.of(), 2, 2);

        assertEquals(List.of("Test post 3", "Test post 2"), page.stream().map(Post::title).toList());
        assertEquals(5, postRepository.count("", List.of()));
    }
}
