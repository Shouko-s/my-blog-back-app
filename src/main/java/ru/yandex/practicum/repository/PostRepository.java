package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(Long id);

    void saveImageForPost(Long postId, byte[] imageBytes);

    byte[] getImageForPost(Long postId);

    List<Post> findPage(String titlePart, List<String> tags, long limit, long offset);

    long count(String titlePart, List<String> tags);
}
