package ru.yandex.practicum.repository.impl;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.PostImage;
import ru.yandex.practicum.repository.ImageRepository;

import java.util.Optional;

@Repository
public class ImageRepositoryImpl implements ImageRepository {
    private final JdbcClient jdbcClient;

    public ImageRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(PostImage image) {
        int updated = jdbcClient.sql("""
                        update post_images set content_type = :contentType, data = :data where post_id = :postId
                        """)
                .param("contentType", image.contentType())
                .param("data", image.data())
                .param("postId", image.postId())
                .update();
        if (updated > 0) {
            return;
        }

        jdbcClient.sql("""
                        insert into post_images(post_id, content_type, data) values(:postId, :contentType, :data)
                        """)
                .param("postId", image.postId())
                .param("contentType", image.contentType())
                .param("data", image.data())
                .update();
    }

    @Override
    public Optional<PostImage> findByPostId(Long postId) {
        return jdbcClient.sql("""
                        select post_id, content_type, data
                        from post_images
                        where post_id = :postId
                        """)
                .param("postId", postId)
                .query(PostImage.class)
                .optional();
    }
}
