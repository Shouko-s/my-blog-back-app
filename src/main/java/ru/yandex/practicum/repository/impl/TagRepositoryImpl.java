package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.repository.TagRepository;

import java.util.*;

@Repository
public class TagRepositoryImpl implements TagRepository {
    private final JdbcClient jdbcClient;

    public TagRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void saveForPost(Long postId, List<String> tags) {
        if (tags.isEmpty()) {
            return;
        }

        for (String tag : tags) {
            jdbcClient.sql("""
                            insert into tags(name)
                            select :name where not exists (select 1 from tags where name = :name)
                            """)
                    .param("name", tag)
                    .update();
        }

        jdbcClient.sql("""
                        insert into post_tags(post_id, tag_id)
                        select :postId, id from tags where name in (:tags)
                        """)
                .param("postId", postId)
                .param("tags", tags)
                .update();
    }

    @Override
    public void deleteForPost(Long postId) {
        jdbcClient.sql("""
                        delete from post_tags where post_id = :postId
                        """)
                .param("postId", postId)
                .update();
    }

    @Override
    public Map<Long, List<String>> findNamesByPostIds(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> tagsByPostId = new HashMap<>();
        jdbcClient.sql("""
                        select pt.post_id, t.name
                        from post_tags pt
                        join tags t on t.id = pt.tag_id
                        where pt.post_id in (:postIds)
                        order by t.name
                        """)
                .param("postIds", postIds)
                .query(rs -> {
                    tagsByPostId.computeIfAbsent(rs.getLong("post_id"), id -> new ArrayList<>())
                            .add(rs.getString("name"));
                });
        return tagsByPostId;
    }
}
