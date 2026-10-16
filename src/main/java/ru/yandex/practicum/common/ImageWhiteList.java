package ru.yandex.practicum.common;

import java.util.Arrays;

public enum ImageWhiteList {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp"),
    GIF("image/gif");

    private final String contentType;

    ImageWhiteList(String contentType) {
        this.contentType = contentType;
    }

    public static boolean isAllowed(String contentType) {
        return Arrays.stream(values())
                .anyMatch(type -> type.contentType.equalsIgnoreCase(contentType));
    }
}
