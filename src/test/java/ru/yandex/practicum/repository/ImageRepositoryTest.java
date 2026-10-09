package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.AbstractIntegrationTest;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.PostImage;

import static org.junit.jupiter.api.Assertions.*;

class ImageRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    void save_shouldStoreImage() {
        Long postId = postRepository.save(new Post(null, "Test title", "Test text", null)).id();

        imageRepository.save(new PostImage(postId, "image/png", new byte[]{1, 2, 3}));

        PostImage found = imageRepository.findByPostId(postId).orElseThrow();
        assertEquals("image/png", found.contentType());
        assertArrayEquals(new byte[]{1, 2, 3}, found.data());
    }

    @Test
    void save_shouldReplaceExistingImage() {
        Long postId = postRepository.save(new Post(null, "Test title", "Test text", null)).id();
        imageRepository.save(new PostImage(postId, "image/png", new byte[]{1}));

        imageRepository.save(new PostImage(postId, "image/jpeg", new byte[]{9, 9}));

        PostImage found = imageRepository.findByPostId(postId).orElseThrow();
        assertEquals("image/jpeg", found.contentType());
        assertArrayEquals(new byte[]{9, 9}, found.data());
    }

    @Test
    void findByPostId_shouldReturnEmpty_whenNoImage() {
        assertTrue(imageRepository.findByPostId(-1L).isEmpty());
    }
}
