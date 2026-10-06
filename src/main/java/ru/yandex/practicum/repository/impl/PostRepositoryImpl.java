package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class PostRepositoryImpl implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    public PostRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        return new Post(id, post.title(), post.text(), 0);
    }

    @Override
    public void saveImageForPost(Long postId, byte[] imageBytes) {
        String query = """
                update posts set image = ? where id = ?
                """;
        jdbcTemplate.update(query, imageBytes, postId);
    }

    @Override
    public byte[] getImageForPost(Long postId) {
        String query = """
                select image from posts where id = ?
                """;
        List<byte[]> images = jdbcTemplate.query(query, (rs, rowNum) -> rs.getBytes("image"), postId);
        return images.isEmpty() ? null : images.getFirst();
    }
}
