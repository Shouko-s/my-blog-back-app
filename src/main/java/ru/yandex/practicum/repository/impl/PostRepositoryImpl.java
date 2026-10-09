package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Post save(Post post) {
        String query = """
                insert into posts(title, text) values(?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, new String[]{"id"});
            ps.setString(1, post.title());
            ps.setString(2, post.text());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Post(id, post.title(), post.text(), 0L);
    }

    @Override
    public boolean update(Post post) {
        String query = """
                update posts set title = ?, text = ? where id = ?
                """;
        return jdbcTemplate.update(query, post.title(), post.text(), post.id()) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        String query = """
                delete from posts where id = ?
                """;
        return jdbcTemplate.update(query, id) > 0;
    }

    @Override
    public boolean incrementLikes(Long id) {
        String query = """
                update posts set likes_count = likes_count + 1 where id = ?
                """;
        return jdbcTemplate.update(query, id) > 0;
    }

    @Override
    public Optional<Long> findLikesCountById(Long id) {
        String query = """
                select likes_count from posts where id = ?
                """;
        return jdbcTemplate.queryForList(query, Long.class, id).stream().findFirst();
    }

    @Override
    public Optional<Post> findById(Long id) {
        String query = """
                select id, title, text, likes_count
                from posts
                where id = ?
                """;
        List<Post> posts = jdbcTemplate.query(query,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getLong("likes_count")),
                id);
        return posts.stream().findFirst();
    }

    @Override
    public boolean existsById(Long id) {
        String query = """
                select exists(select 1 from posts where id = ?)
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, id));
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
        return namedParameterJdbcTemplate.query(query, params,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getLong("likes_count")));
    }

    @Override
    public long count(String titlePart, List<String> tags) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String query = """
                select count(*)
                from posts p
                %s
                """.formatted(buildWhereClause(titlePart, tags, params));
        Long count = namedParameterJdbcTemplate.queryForObject(query, params, Long.class);
        return count == null ? 0 : count;
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
