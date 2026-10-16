package ru.yandex.practicum.common;

import java.time.LocalDateTime;

public record CustomErrorResponse(
        int code,
        String status,
        String message,
        LocalDateTime timestamp
) {
}
