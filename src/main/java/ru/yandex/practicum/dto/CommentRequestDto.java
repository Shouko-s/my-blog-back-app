package ru.yandex.practicum.dto;

public record CommentRequestDto(
        Long id,
        String text,
        Long postId
) {
}
