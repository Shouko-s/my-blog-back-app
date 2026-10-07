package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.repository.TagRepository;

import java.util.*;

@Repository
public class TagRepositoryImpl implements TagRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void saveForPost(Long postId, List<String> tags) {
        if (tags.isEmpty()) {
            return;
        }

        String insertTagQuery = """
                insert into tags(name)
                select ? where not exists (select 1 from tags where name = ?)
                """;
        jdbcTemplate.batchUpdate(insertTagQuery, tags, tags.size(), (ps, tag) -> {
            ps.setString(1, tag);
            ps.setString(2, tag);
        });

        String linkQuery = """
                insert into post_tags(post_id, tag_id)
                select ?, id from tags where name = ?
                """;
        jdbcTemplate.batchUpdate(linkQuery, tags, tags.size(), (ps, tag) -> {
            ps.setLong(1, postId);
            ps.setString(2, tag);
        });
    }

    @Override
    public void deleteForPost(Long postId) {
        String query = """
                delete from post_tags where post_id = ?
                """;
        jdbcTemplate.update(query, postId);
    }

    @Override
    public Map<Long, List<String>> findNamesByPostIds(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        String query = """
                select pt.post_id, t.name
                from post_tags pt
                join tags t on t.id = pt.tag_id
                where pt.post_id in (:postIds)
                order by t.name
                """;
        Map<Long, List<String>> tagsByPostId = new HashMap<>();
        namedParameterJdbcTemplate.query(query, Map.of("postIds", postIds), rs -> {
            tagsByPostId.computeIfAbsent(rs.getLong("post_id"), id -> new ArrayList<>())
                    .add(rs.getString("name"));
        });
        return tagsByPostId;
    }
}
