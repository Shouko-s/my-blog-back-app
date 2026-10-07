package ru.yandex.practicum.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CommentRequestDto;
import ru.yandex.practicum.dto.CommentResponseDto;
import ru.yandex.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponseDto> findAll(@PathVariable("postId") Long postId) {
        return commentService.findAllByPostId(postId);
    }

    @GetMapping("/{commentId}")
    public CommentResponseDto findById(@PathVariable("postId") Long postId,
                                       @PathVariable("commentId") Long commentId) {
        return commentService.findById(postId, commentId);
    }

    @PostMapping
    public CommentResponseDto create(@PathVariable("postId") Long postId,
                                     @RequestBody CommentRequestDto requestDto) {
        return commentService.save(postId, requestDto);
    }

    @PutMapping("/{commentId}")
    public CommentResponseDto update(@PathVariable("postId") Long postId,
                                     @PathVariable("commentId") Long commentId,
                                     @RequestBody CommentRequestDto requestDto) {
        return commentService.update(postId, commentId, requestDto);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable("postId") Long postId,
                       @PathVariable("commentId") Long commentId) {
        commentService.delete(postId, commentId);
    }
}
