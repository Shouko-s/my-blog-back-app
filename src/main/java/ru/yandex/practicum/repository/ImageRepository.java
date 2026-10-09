package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.PostImage;

import java.util.Optional;

public interface ImageRepository {
    void save(PostImage image);

    Optional<PostImage> findByPostId(Long postId);
}
