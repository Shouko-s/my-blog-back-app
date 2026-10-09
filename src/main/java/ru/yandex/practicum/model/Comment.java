package ru.yandex.practicum.model;

public record Comment(
        Long id,
        String text,
        Long postId
) {
}
