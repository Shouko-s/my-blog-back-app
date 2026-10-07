package ru.yandex.practicum.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TagRepository {
    void saveForPost(Long postId, List<String> tags);

    void deleteForPost(Long postId);

    Map<Long, List<String>> findNamesByPostIds(Collection<Long> postIds);
}
