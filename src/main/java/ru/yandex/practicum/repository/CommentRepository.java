package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Comment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment comment);

    boolean update(Comment comment);

    boolean deleteByIdAndPostId(Long id, Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    List<Comment> findByPostId(Long postId);

    Map<Long, Long> countByPostIds(Collection<Long> postIds);
}
