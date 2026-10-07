package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.dto.CommentRequestDto;
import ru.yandex.practicum.dto.CommentResponseDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest extends AbstractServiceTest {

    @Autowired
    private CommentService commentService;

    @Test
    void findAllByPostId_shouldReturnComments() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPostId(1L)).thenReturn(List.of(new Comment(10L, "Test comment", 1L)));

        assertEquals(List.of(new CommentResponseDto(10L, "Test comment", 1L)), commentService.findAllByPostId(1L));
    }

    @Test
    void findAllByPostId_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> commentService.findAllByPostId(1L));
        verifyNoInteractions(commentRepository);
    }

    @Test
    void findById_shouldThrowNotFound_whenCommentDoesNotExist() {
        when(commentRepository.findByIdAndPostId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.findById(1L, 10L));
    }

    @Test
    void save_shouldSaveCommentForPostFromPath() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.save(any())).thenReturn(new Comment(10L, "Test comment", 1L));

        CommentResponseDto result = commentService.save(1L, new CommentRequestDto(null, "Test comment", 1L));

        assertEquals(new CommentResponseDto(10L, "Test comment", 1L), result);
        verify(commentRepository).save(new Comment(null, "Test comment", 1L));
    }

    @Test
    void save_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.save(1L, new CommentRequestDto(null, "Test comment", 1L)));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateComment() {
        when(commentRepository.update(any())).thenReturn(true);

        CommentResponseDto result = commentService.update(1L, 10L, new CommentRequestDto(10L, "Updated test comment", 1L));

        assertEquals(new CommentResponseDto(10L, "Updated test comment", 1L), result);
        verify(commentRepository).update(new Comment(10L, "Updated test comment", 1L));
    }

    @Test
    void update_shouldThrowNotFound_whenCommentDoesNotExist() {
        when(commentRepository.update(any())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.update(1L, 10L, new CommentRequestDto(10L, "Updated test comment", 1L)));
    }

    @Test
    void delete_shouldThrowNotFound_whenCommentDoesNotExist() {
        when(commentRepository.deleteByIdAndPostId(10L, 1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> commentService.delete(1L, 10L));
    }
}
