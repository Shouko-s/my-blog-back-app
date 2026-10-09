package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);

    boolean update(Post post);

    boolean deleteById(Long id);

    boolean incrementLikes(Long id);

    Optional<Long> findLikesCountById(Long id);

    Optional<Post> findById(Long id);

    boolean existsById(Long id);

    List<Post> findPage(String titlePart, List<String> tags, long limit, long offset);

    long count(String titlePart, List<String> tags);
}
