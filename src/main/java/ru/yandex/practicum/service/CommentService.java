package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.CommentRequestDto;
import ru.yandex.practicum.dto.CommentResponseDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> findAllByPostId(Long postId) {
        checkPostExists(postId);
        return commentRepository.findByPostId(postId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CommentResponseDto findById(Long postId, Long commentId) {
        return commentRepository.findByIdAndPostId(commentId, postId)
                .map(this::toDto)
                .orElseThrow(() -> commentNotFound(postId, commentId));
    }

    @Transactional
    public CommentResponseDto save(Long postId, CommentRequestDto requestDto) {
        checkPostExists(postId);
        Comment comment = commentRepository.save(new Comment(null, requestDto.text(), postId));
        return toDto(comment);
    }

    @Transactional
    public CommentResponseDto update(Long postId, Long commentId, CommentRequestDto requestDto) {
        Comment comment = new Comment(commentId, requestDto.text(), postId);
        if (!commentRepository.update(comment)) {
            throw commentNotFound(postId, commentId);
        }
        return toDto(comment);
    }

    @Transactional
    public void delete(Long postId, Long commentId) {
        if (!commentRepository.deleteByIdAndPostId(commentId, postId)) {
            throw commentNotFound(postId, commentId);
        }
    }

    private void checkPostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post with id " + postId + " not found");
        }
    }

    private NotFoundException commentNotFound(Long postId, Long commentId) {
        return new NotFoundException("Comment with id " + commentId + " for post with id " + postId + " not found");
    }

    private CommentResponseDto toDto(Comment comment) {
        return new CommentResponseDto(comment.id(), comment.text(), comment.postId());
    }
}
