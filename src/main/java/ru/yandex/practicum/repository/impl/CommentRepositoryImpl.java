package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
public class CommentRepositoryImpl implements CommentRepository {
    private static final RowMapper<Comment> COMMENT_ROW_MAPPER = (rs, rowNum) -> new Comment(
            rs.getLong("id"),
            rs.getString("text"),
            rs.getLong("post_id"));

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CommentRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Comment save(Comment comment) {
        String query = """
                insert into comments(post_id, text) values(?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, new String[]{"id"});
            ps.setLong(1, comment.postId());
            ps.setString(2, comment.text());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Comment(id, comment.text(), comment.postId());
    }

    @Override
    public boolean update(Comment comment) {
        String query = """
                update comments set text = ? where id = ? and post_id = ?
                """;
        return jdbcTemplate.update(query, comment.text(), comment.id(), comment.postId()) > 0;
    }

    @Override
    public boolean deleteByIdAndPostId(Long id, Long postId) {
        String query = """
                delete from comments where id = ? and post_id = ?
                """;
        return jdbcTemplate.update(query, id, postId) > 0;
    }

    @Override
    public Optional<Comment> findByIdAndPostId(Long id, Long postId) {
        String query = """
                select id, post_id, text
                from comments
                where id = ? and post_id = ?
                """;
        return jdbcTemplate.query(query, COMMENT_ROW_MAPPER, id, postId).stream().findFirst();
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        String query = """
                select id, post_id, text
                from comments
                where post_id = ?
                order by id
                """;
        return jdbcTemplate.query(query, COMMENT_ROW_MAPPER, postId);
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
