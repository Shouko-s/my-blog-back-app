package ru.yandex.practicum.model;

public record PostImage(
        Long postId,
        String contentType,
        byte[] data
) {
}
