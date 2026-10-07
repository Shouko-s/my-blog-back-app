package ru.yandex.practicum.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.PostPageResponseDto;
import ru.yandex.practicum.dto.PostRequestDto;
import ru.yandex.practicum.dto.PostResponseDto;
import ru.yandex.practicum.model.PostImage;
import ru.yandex.practicum.service.PostService;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public PostResponseDto create(@RequestBody PostRequestDto requestDto) {
        return postService.save(requestDto);
    }

    @GetMapping("/{postId}")
    public PostResponseDto findById(@PathVariable("postId") Long postId) {
        return postService.findById(postId);
    }

    @PutMapping("/{postId}/image")
    public void saveImageForPost(@PathVariable("postId") Long postId, @RequestParam("image") MultipartFile image) {
        try {
            postService.saveImageForPost(postId, image.getContentType(), image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<byte[]> getImageForPost(@PathVariable("postId") Long postId) {
        PostImage image = postService.getImageForPost(postId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                .body(image.data());
    }

    @GetMapping
    public PostPageResponseDto findAll(@RequestParam("search") String search,
                                       @RequestParam("pageNumber") Long pageNumber,
                                       @RequestParam("pageSize") Long pageSize) {
        return postService.findAllPageable(search, pageNumber, pageSize);
    }
}
