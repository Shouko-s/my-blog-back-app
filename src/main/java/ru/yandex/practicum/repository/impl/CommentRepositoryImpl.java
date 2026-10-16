package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;

import java.util.*;

@Repository
public class CommentRepositoryImpl implements CommentRepository {
    private final JdbcClient jdbcClient;

    public CommentRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Comment save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql("""
                        insert into comments(post_id, text) values(:postId, :text)
                        """)
                .param("postId", comment.postId())
                .param("text", comment.text())
                .update(keyHolder, "id");

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return new Comment(id, comment.text(), comment.postId());
    }

    @Override
    public boolean update(Comment comment) {
        return jdbcClient.sql("""
                        update comments set text = :text where id = :id and post_id = :postId
                        """)
                .param("text", comment.text())
                .param("id", comment.id())
                .param("postId", comment.postId())
                .update() > 0;
    }

    @Override
    public boolean deleteByIdAndPostId(Long id, Long postId) {
        return jdbcClient.sql("""
                        delete from comments where id = :id and post_id = :postId
                        """)
                .param("id", id)
                .param("postId", postId)
                .update() > 0;
    }

    @Override
    public Optional<Comment> findByIdAndPostId(Long id, Long postId) {
        return jdbcClient.sql("""
                        select id, post_id, text
                        from comments
                        where id = :id and post_id = :postId
                        """)
                .param("id", id)
                .param("postId", postId)
                .query(Comment.class)
                .optional();
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return jdbcClient.sql("""
                        select id, post_id, text
                        from comments
                        where post_id = :postId
                        order by id
                        """)
                .param("postId", postId)
                .query(Comment.class)
                .list();
    }

    @Override
    public Map<Long, Long> countByPostIds(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> countByPostId = new HashMap<>();
        jdbcClient.sql("""
                        select post_id, count(*) as comments_count
                        from comments
                        where post_id in (:postIds)
                        group by post_id
                        """)
                .param("postIds", postIds)
                .query(rs -> {
                    countByPostId.put(rs.getLong("post_id"), rs.getLong("comments_count"));
                });
        return countByPostId;
    }
}
