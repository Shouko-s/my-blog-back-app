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
        String updateQuery = """
                update post_images set content_type = ?, data = ? where post_id = ?
                """;
        int updated = jdbcTemplate.update(updateQuery, image.contentType(), image.data(), image.postId());
        if (updated > 0) {
            return;
        }

        String insertQuery = """
                insert into post_images(post_id, content_type, data) values(?, ?, ?)
                """;
        jdbcTemplate.update(insertQuery, image.postId(), image.contentType(), image.data());
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
