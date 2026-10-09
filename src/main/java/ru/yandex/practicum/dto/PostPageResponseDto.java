package ru.yandex.practicum.dto;

import java.util.List;

public record PostPageResponseDto(
        List<PostResponseDto> posts,
        boolean hasPrev,
        boolean hasNext,
        Long lastPage
) {
}
