package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.repository.TagRepository;

import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AbstractServiceTest.TestConfig.class)
abstract class AbstractServiceTest {

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected ImageRepository imageRepository;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(postRepository, tagRepository, commentRepository, imageRepository);
    }

    static class TestConfig {

        @Bean
        public PostRepository postRepository() {
            return mock(PostRepository.class);
        }

        @Bean
        public TagRepository tagRepository() {
            return mock(TagRepository.class);
        }

        @Bean
        public CommentRepository commentRepository() {
            return mock(CommentRepository.class);
        }

        @Bean
        public ImageRepository imageRepository() {
            return mock(ImageRepository.class);
        }

        @Bean
        public PostService postService(PostRepository postRepository, TagRepository tagRepository,
                                       CommentRepository commentRepository, ImageRepository imageRepository) {
            return new PostService(postRepository, tagRepository, commentRepository, imageRepository);
        }

        @Bean
        public CommentService commentService(CommentRepository commentRepository, PostRepository postRepository) {
            return new CommentService(commentRepository, postRepository);
        }
    }
}
