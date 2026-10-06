package ru.yandex.practicum.dto;

import java.util.List;

public record PostRequestDto(
        Long id,
        String title,
        String text,
        List<String> tags
) {
}
