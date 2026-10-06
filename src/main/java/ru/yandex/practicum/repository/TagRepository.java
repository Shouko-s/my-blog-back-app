package ru.yandex.practicum.repository;

import java.util.List;

public interface TagRepository {
    void saveForPost(Long postId, List<String> tags);
}
