package ru.yandex.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.AbstractIntegrationTest;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private Long postId;

    @BeforeEach
    void setUp() {
        postId = postRepository.save(new Post(null, "Test title", "Test text", null)).id();
    }

    @Test
    void save_shouldGenerateId() {
        Comment saved = commentRepository.save(new Comment(null, "Test comment", postId));

        assertNotNull(saved.id());
        assertEquals(saved, commentRepository.findByIdAndPostId(saved.id(), postId).orElseThrow());
    }

    @Test
    void findByIdAndPostId_shouldReturnEmpty_whenCommentBelongsToAnotherPost() {
        Long otherPostId = postRepository.save(new Post(null, "Other test post", "Test text", null)).id();
        Comment saved = commentRepository.save(new Comment(null, "Test comment", postId));

        assertTrue(commentRepository.findByIdAndPostId(saved.id(), otherPostId).isEmpty());
    }

    @Test
    void findByPostId_shouldReturnCommentsInCreationOrder() {
        Comment first = commentRepository.save(new Comment(null, "Test comment 1", postId));
        Comment second = commentRepository.save(new Comment(null, "Test comment 2", postId));

        assertEquals(List.of(first, second), commentRepository.findByPostId(postId));
    }

    @Test
    void update_shouldChangeText() {
        Comment saved = commentRepository.save(new Comment(null, "Test comment", postId));

        assertTrue(commentRepository.update(new Comment(saved.id(), "Updated test comment", postId)));
        assertEquals("Updated test comment", commentRepository.findByIdAndPostId(saved.id(), postId).orElseThrow().text());
    }

    @Test
    void update_shouldReturnFalse_whenCommentDoesNotExist() {
        assertFalse(commentRepository.update(new Comment(-1L, "Test text", postId)));
    }

    @Test
    void deleteByIdAndPostId_shouldRemoveComment() {
        Comment saved = commentRepository.save(new Comment(null, "Test comment", postId));

        assertTrue(commentRepository.deleteByIdAndPostId(saved.id(), postId));
        assertTrue(commentRepository.findByPostId(postId).isEmpty());
    }

    @Test
    void countByPostIds_shouldCountCommentsPerPost() {
        Long otherPostId = postRepository.save(new Post(null, "Other test post", "Test text", null)).id();
        commentRepository.save(new Comment(null, "Test comment 1", postId));
        commentRepository.save(new Comment(null, "Test comment 2", postId));
        commentRepository.save(new Comment(null, "Test comment 3", otherPostId));

        assertEquals(Map.of(postId, 2L, otherPostId, 1L),
                commentRepository.countByPostIds(List.of(postId, otherPostId)));
    }

    @Test
    void deletingPost_shouldCascadeDeleteComments() {
        commentRepository.save(new Comment(null, "Test comment", postId));

        postRepository.deleteById(postId);

        assertTrue(commentRepository.findByPostId(postId).isEmpty());
    }
}
