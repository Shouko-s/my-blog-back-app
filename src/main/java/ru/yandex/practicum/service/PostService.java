package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.PostPageResponseDto;
import ru.yandex.practicum.dto.PostRequestDto;
import ru.yandex.practicum.dto.PostResponseDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.PostImage;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.ImageRepository;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.repository.TagRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostService {
    private static final int PREVIEW_TEXT_MAX_LENGTH = 128;

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;

    public PostService(PostRepository postRepository, TagRepository tagRepository,
                       CommentRepository commentRepository, ImageRepository imageRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public PostResponseDto save(PostRequestDto requestDto) {
        Post post = postRepository.save(new Post(null, requestDto.title(), requestDto.text(), 0L));
        List<String> tags = normalizeTags(requestDto.tags());
        tagRepository.saveForPost(post.id(), tags);
        return new PostResponseDto(post.id(), post.title(), post.text(), tags, post.likesCount(), 0L);
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post with id " + postId + " not found"));
        List<String> tags = tagRepository.findNamesByPostIds(List.of(postId))
                .getOrDefault(postId, List.of());
        Long commentsCount = commentRepository.countByPostIds(List.of(postId))
                .getOrDefault(postId, 0L);
        return new PostResponseDto(post.id(), post.title(), post.text(), tags, post.likesCount(), commentsCount);
    }

    @Transactional
    public void saveImageForPost(Long postId, String contentType, byte[] imageBytes) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post with id " + postId + " not found");
        }
        imageRepository.save(new PostImage(postId, contentType, imageBytes));
    }

    public PostImage getImageForPost(Long postId) {
        return imageRepository.findByPostId(postId)
                .orElseThrow(() -> new NotFoundException("Image for post with id " + postId + " not found"));
    }

    @Transactional(readOnly = true)
    public PostPageResponseDto findAllPageable(String search, Long pageNumber, Long pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be greater than 0");
        }
        if (pageSize == null || pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }

        List<String> words = Arrays.stream(search.trim().split("\\s+"))
                .filter(w -> !w.isEmpty())
                .toList();
        List<String> tags = resolveTags(words);
        String titlePart = resolveTitlePart(words);

        long total = postRepository.count(titlePart, tags);
        long lastPage = Math.max(1, (total + pageSize - 1) / pageSize);
        long currentPage = Math.min(pageNumber, lastPage);
        long offset = (currentPage - 1) * pageSize;

        List<Post> posts = postRepository.findPage(titlePart, tags, pageSize, offset);
        List<PostResponseDto> postResponseDtos = toBuildDtoList(posts);

        return new PostPageResponseDto(postResponseDtos, currentPage > 1, currentPage < lastPage, lastPage);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();
    }

    private List<PostResponseDto> toBuildDtoList(List<Post> posts) {
        List<Long> postIds = posts.stream().map(Post::id).toList();
        Map<Long, List<String>> tagsByPostId = tagRepository.findNamesByPostIds(postIds);
        Map<Long, Long> commentsCountByPostId = commentRepository.countByPostIds(postIds);

        List<PostResponseDto> dtos = new ArrayList<>();
        for (Post post : posts) {
            dtos.add(
                    new PostResponseDto(
                            post.id(),
                            post.title(),
                            truncateText(post.text()),
                            tagsByPostId.getOrDefault(post.id(), List.of()),
                            post.likesCount(),
                            commentsCountByPostId.getOrDefault(post.id(), 0L))
            );
        }
        return dtos;
    }

    private String truncateText(String text) {
        if (text.length() <= PREVIEW_TEXT_MAX_LENGTH) {
            return text;
        }
        return text.substring(0, PREVIEW_TEXT_MAX_LENGTH) + "…";
    }

    private List<String> resolveTags(List<String> words) {
        return words.stream()
                .filter(w -> w.startsWith("#"))
                .map(w -> w.substring(1))
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .toList();
    }

    private String resolveTitlePart(List<String> words) {
        return words.stream()
                .filter(w -> !w.startsWith("#"))
                .collect(Collectors.joining(" "));
    }
}
