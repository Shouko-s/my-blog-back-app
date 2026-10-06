package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.repository.TagRepository;

import java.util.List;

@Repository
public class TagRepositoryImpl implements TagRepository {
    private final JdbcTemplate jdbcTemplate;

    public TagRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
