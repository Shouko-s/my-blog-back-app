package ru.yandex.practicum.model;

public record Post(
        Long id,
        String title,
        String text,
        Long likesCount
) {
}
