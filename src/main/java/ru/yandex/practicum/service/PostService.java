package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.PostRequestDto;
import ru.yandex.practicum.dto.PostResponseDto;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.repository.TagRepository;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository postRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public PostResponseDto save(PostRequestDto requestDto) {
        Post post = postRepository.save(new Post(null, requestDto.title(), requestDto.text(), 0));
        List<String> tags = normalizeTags(requestDto.tags());
        tagRepository.saveForPost(post.id(), tags);
        return new PostResponseDto(post.id(), post.title(), post.text(), tags, post.likesCount(), 0);
    }

    public void saveImageForPost(Long postId, byte[] imageBytes) {
        postRepository.saveImageForPost(postId, imageBytes);
    }

    public byte[] getImageForPost(Long postId) {
        return postRepository.getImageForPost(postId);
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
}
