package ru.yandex.practicum.dto;

public record CommentResponseDto(
        Long id,
        String text,
        Long postId
) {
}
