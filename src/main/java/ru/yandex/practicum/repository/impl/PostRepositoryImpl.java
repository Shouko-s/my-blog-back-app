package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PostRepositoryImpl implements PostRepository {
    private final JdbcClient jdbcClient;

    public PostRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Post save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                        insert into posts(title, text) values(:title, :text)
                        """)
                .param("title", post.title())
                .param("text", post.text())
                .update(keyHolder, "id");

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Post(id, post.title(), post.text(), 0L);
    }

    @Override
    public boolean update(Post post) {
        return jdbcClient.sql("""
                        update posts set title = :title, text = :text where id = :id
                        """)
                .param("title", post.title())
                .param("text", post.text())
                .param("id", post.id())
                .update() > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return jdbcClient.sql("""
                        delete from posts where id = :id
                        """)
                .param("id", id)
                .update() > 0;
    }

    @Override
    public boolean incrementLikes(Long id) {
        return jdbcClient.sql("""
                        update posts set likes_count = likes_count + 1 where id = :id
                        """)
                .param("id", id)
                .update() > 0;
    }

    @Override
    public Optional<Long> findLikesCountById(Long id) {
        return jdbcClient.sql("""
                        select likes_count from posts where id = :id
                        """)
                .param("id", id)
                .query(Long.class)
                .optional();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return jdbcClient.sql("""
                        select id, title, text, likes_count
                        from posts
                        where id = :id
                        """)
                .param("id", id)
                .query(Post.class)
                .optional();
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcClient.sql("""
                        select exists(select 1 from posts where id = :id)
                        """)
                .param("id", id)
                .query(Boolean.class)
                .single();
    }

    @Override
    public List<Post> findPage(String titlePart, List<String> tags, long limit, long offset) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        String query = """
                select p.id, p.title, p.text, p.likes_count
                from posts p
                %s
                order by p.id desc
                limit :limit offset :offset
                """.formatted(buildWhereClause(titlePart, tags, params));
        return jdbcClient.sql(query)
                .paramSource(params)
                .query(Post.class)
                .list();
    }

    @Override
    public long count(String titlePart, List<String> tags) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String query = """
                select count(*)
                from posts p
                %s
                """.formatted(buildWhereClause(titlePart, tags, params));
        return jdbcClient.sql(query)
                .paramSource(params)
                .query(Long.class)
                .single();
    }

    private String buildWhereClause(String titlePart, List<String> tags, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();

        if (!titlePart.isEmpty()) {
            conditions.add("lower(p.title) like lower(:title) escape '\\'");
            params.addValue("title", "%" + escapeLikePattern(titlePart) + "%");
        }

        if (!tags.isEmpty()) {
            conditions.add("""
                    p.id in (
                        select pt.post_id
                        from post_tags pt
                        join tags t on t.id = pt.tag_id
                        where t.name in (:tags)
                        group by pt.post_id
                        having count(distinct t.name) = :tagsCount
                    )""");
            params.addValue("tags", tags);
            params.addValue("tagsCount", tags.size());
        }

        return conditions.isEmpty() ? "" : "where " + String.join(" and ", conditions);
    }

    private String escapeLikePattern(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
