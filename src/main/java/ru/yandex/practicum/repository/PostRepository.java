package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Post;

public interface PostRepository {
    Post save(Post post);

    void saveImageForPost(Long postId, byte[] imageBytes);

    byte[] getImageForPost(Long postId);
}
