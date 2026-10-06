package ru.yandex.practicum.repository;

import java.util.Collection;
import java.util.Map;

public interface CommentRepository {
    Map<Long, Long> countByPostIds(Collection<Long> postIds);
}
