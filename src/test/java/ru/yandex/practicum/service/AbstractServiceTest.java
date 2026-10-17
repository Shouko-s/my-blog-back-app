package ru.yandex.practicum.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.repository.TagRepository;

@SpringBootTest(classes = {PostService.class, CommentService.class})
abstract class AbstractServiceTest {

    @MockitoBean
    protected PostRepository postRepository;

    @MockitoBean
    protected TagRepository tagRepository;

    @MockitoBean
    protected CommentRepository commentRepository;

    @MockitoBean
    protected ImageRepository imageRepository;
}
