package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.repository.CommentRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class CommentRepositoryImpl implements CommentRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CommentRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Map<Long, Long> countByPostIds(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        String query = """
                select post_id, count(*) as comments_count
                from comments
                where post_id in (:postIds)
                group by post_id
                """;
        Map<Long, Long> countByPostId = new HashMap<>();
        namedParameterJdbcTemplate.query(query, Map.of("postIds", postIds), rs -> {
            countByPostId.put(rs.getLong("post_id"), rs.getLong("comments_count"));
        });
        return countByPostId;
    }
}
