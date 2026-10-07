package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.PostImage;
import ru.yandex.practicum.repository.ImageRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class ImageRepositoryImpl implements ImageRepository {
    private final JdbcTemplate jdbcTemplate;

    public ImageRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(PostImage image) {
        String query = """
                insert into post_images(post_id, content_type, data) values(?, ?, ?)
                on conflict (post_id) do update
                set content_type = excluded.content_type, data = excluded.data
                """;
        jdbcTemplate.update(query, image.postId(), image.contentType(), image.data());
    }

    @Override
    public Optional<PostImage> findByPostId(Long postId) {
        String query = """
                select post_id, content_type, data
                from post_images
                where post_id = ?
                """;
        List<PostImage> images = jdbcTemplate.query(query,
                (rs, rowNum) -> new PostImage(
                        rs.getLong("post_id"),
                        rs.getString("content_type"),
                        rs.getBytes("data")),
                postId);
        return images.stream().findFirst();
    }
}
