package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.dto.PostPageResponseDto;
import ru.yandex.practicum.dto.PostRequestDto;
import ru.yandex.practicum.dto.PostResponseDto;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.model.PostImage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostServiceTest extends AbstractServiceTest {

    @Autowired
    private PostService postService;

    @Test
    void save_shouldSavePostWithNormalizedTags() {
        when(postRepository.save(any())).thenReturn(new Post(1L, "Test title", "Test text", 0L));

        PostResponseDto result = postService.save(
                new PostRequestDto(null, "Test title", "Test text", List.of(" test_tag_1 ", "", "test_tag_2", "test_tag_1")));

        assertEquals(new PostResponseDto(1L, "Test title", "Test text", List.of("test_tag_1", "test_tag_2"), 0L, 0L), result);
        verify(tagRepository).saveForPost(1L, List.of("test_tag_1", "test_tag_2"));
    }

    @Test
    void save_shouldAcceptNullTags() {
        when(postRepository.save(any())).thenReturn(new Post(1L, "Test title", "Test text", 0L));

        PostResponseDto result = postService.save(new PostRequestDto(null, "Test title", "Test text", null));

        assertEquals(List.of(), result.tags());
        verify(tagRepository).saveForPost(1L, List.of());
    }

    @Test
    void findById_shouldAssemblePostWithTagsAndCommentsCount() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(new Post(1L, "Test title", "a".repeat(200), 5L)));
        when(tagRepository.findNamesByPostIds(List.of(1L))).thenReturn(Map.of(1L, List.of("test_tag_1")));
        when(commentRepository.countByPostIds(List.of(1L))).thenReturn(Map.of(1L, 3L));

        PostResponseDto result = postService.findById(1L);

        assertEquals(new PostResponseDto(1L, "Test title", "a".repeat(200), List.of("test_tag_1"), 5L, 3L), result);
    }

    @Test
    void findById_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.findById(1L));
    }

    @Test
    void update_shouldReplaceTags() {
        when(postRepository.update(any())).thenReturn(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(new Post(1L, "Updated test title", "Test text", 0L)));

        postService.update(1L, new PostRequestDto(1L, "Updated test title", "Test text", List.of("updated_test_tag")));

        verify(postRepository).update(new Post(1L, "Updated test title", "Test text", null));
        verify(tagRepository).deleteForPost(1L);
        verify(tagRepository).saveForPost(1L, List.of("updated_test_tag"));
    }

    @Test
    void update_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.update(any())).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> postService.update(1L, new PostRequestDto(1L, "Test title", "Test text", List.of())));
        verifyNoInteractions(tagRepository);
    }

    @Test
    void delete_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.deleteById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> postService.delete(1L));
    }

    @Test
    void incrementLikes_shouldReturnUpdatedLikesCount() {
        when(postRepository.incrementLikes(1L)).thenReturn(true);
        when(postRepository.findLikesCountById(1L)).thenReturn(Optional.of(6L));

        assertEquals(6L, postService.incrementLikes(1L));
    }

    @Test
    void incrementLikes_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.incrementLikes(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> postService.incrementLikes(1L));
    }

    @Test
    void saveImageForPost_shouldThrowNotFound_whenPostDoesNotExist() {
        when(postRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> postService.saveImageForPost(1L, "image/png", new byte[]{1}));
        verifyNoInteractions(imageRepository);
    }

    @Test
    void saveImageForPost_shouldSaveImage() {
        when(postRepository.existsById(1L)).thenReturn(true);
        byte[] bytes = {1, 2};

        postService.saveImageForPost(1L, "image/png", bytes);

        verify(imageRepository).save(argThat(image ->
                image.postId() == 1L && image.contentType().equals("image/png") && image.data() == bytes));
    }

    @Test
    void getImageForPost_shouldThrowNotFound_whenNoImage() {
        when(imageRepository.findByPostId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> postService.getImageForPost(1L));
    }

    @Test
    void getImageForPost_shouldReturnImage() {
        PostImage image = new PostImage(1L, "image/png", new byte[]{1});
        when(imageRepository.findByPostId(1L)).thenReturn(Optional.of(image));

        assertSame(image, postService.getImageForPost(1L));
    }

    @Test
    void findAllPageable_shouldSplitSearchIntoTitleAndTags() {
        when(postRepository.count(anyString(), anyList())).thenReturn(0L);
        when(postRepository.findPage(anyString(), anyList(), anyLong(), anyLong())).thenReturn(List.of());

        postService.findAllPageable("  test #test_tag_1   title #test_tag_2 #test_tag_1 ", 1L, 5L);

        verify(postRepository).count("test title", List.of("test_tag_1", "test_tag_2"));
        verify(postRepository).findPage("test title", List.of("test_tag_1", "test_tag_2"), 5L, 0L);
    }

    @Test
    void findAllPageable_shouldTruncateTextAndCalculatePagination() {
        Post longPost = new Post(1L, "Test title", "a".repeat(200), 2L);
        when(postRepository.count("", List.of())).thenReturn(11L);
        when(postRepository.findPage("", List.of(), 5L, 5L)).thenReturn(List.of(longPost));
        when(tagRepository.findNamesByPostIds(List.of(1L))).thenReturn(Map.of(1L, List.of("test_tag_1")));
        when(commentRepository.countByPostIds(List.of(1L))).thenReturn(Map.of());

        PostPageResponseDto result = postService.findAllPageable("", 2L, 5L);

        assertEquals(List.of(new PostResponseDto(1L, "Test title", "a".repeat(128) + "…", List.of("test_tag_1"), 2L, 0L)),
                result.posts());
        assertTrue(result.hasPrev());
        assertTrue(result.hasNext());
        assertEquals(3L, result.lastPage());
    }

    @Test
    void findAllPageable_shouldNotTruncateTextOf128Chars() {
        Post post = new Post(1L, "Test title", "a".repeat(128), 0L);
        when(postRepository.count("", List.of())).thenReturn(1L);
        when(postRepository.findPage("", List.of(), 5L, 0L)).thenReturn(List.of(post));

        PostPageResponseDto result = postService.findAllPageable("", 1L, 5L);

        assertEquals("a".repeat(128), result.posts().getFirst().text());
        assertFalse(result.hasPrev());
        assertFalse(result.hasNext());
        assertEquals(1L, result.lastPage());
    }

    @Test
    void findAllPageable_shouldReturnFirstPage_whenNoPosts() {
        when(postRepository.count("", List.of())).thenReturn(0L);
        when(postRepository.findPage("", List.of(), 5L, 0L)).thenReturn(List.of());

        PostPageResponseDto result = postService.findAllPageable("", 1L, 5L);

        assertTrue(result.posts().isEmpty());
        assertFalse(result.hasPrev());
        assertFalse(result.hasNext());
        assertEquals(1L, result.lastPage());
    }

    @Test
    void findAllPageable_shouldRejectInvalidPaging() {
        assertThrows(IllegalArgumentException.class, () -> postService.findAllPageable("", 0L, 5L));
        assertThrows(IllegalArgumentException.class, () -> postService.findAllPageable("", 1L, 0L));
    }
}
