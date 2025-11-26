package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Bookmark;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.service.BookmarkService;
import com.example.boardpjt.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostApiController {

    private final PostService postService;
    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<PostDTO.Response> createPost(@RequestBody PostDTO.Request dto, Authentication authentication) {
        Post post = postService.createPost(dto, authentication.getName());
        long likeCount = postService.getLikeCount(post);
        return new ResponseEntity<>(PostDTO.Response.from(post, likeCount), HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId, @RequestBody PostDTO.Request dto, Authentication authentication) {
        postService.updatePost(postId, dto, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Authentication authentication) {
        postService.deleteById(postId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO.Response> getPost(@PathVariable Long postId) {
        Post post = postService.findById(postId);
        long likeCount = postService.getLikeCount(post);
        return ResponseEntity.ok(PostDTO.Response.from(post, likeCount));
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO.Response>> getPosts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Page<Post> postPage = postService.findWithPagingAndSearch("", page);
        Page<PostDTO.Response> responsePage = postPage.map(post -> PostDTO.Response.from(post, postService.getLikeCount(post)));
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDTO.Response>> searchPostsByTag(@RequestParam String tag, @RequestParam(defaultValue = "0") int page) {
        Page<Post> postPage = postService.findByTag(tag, page);
        Page<PostDTO.Response> responsePage = postPage.map(post -> PostDTO.Response.from(post, postService.getLikeCount(post)));
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId, Authentication authentication) {
        long likeCount = postService.toggleLike(postId, authentication.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "게시글 좋아요를 처리했습니다.");
        response.put("likeCount", likeCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/bookmark")
    public ResponseEntity<Map<String, String>> toggleBookmark(@PathVariable Long postId, Authentication authentication) {
        bookmarkService.toggleBookmark(postId, authentication.getName());
        Map<String, String> response = new HashMap<>();
        response.put("message", "북마크를 처리했습니다.");
        return ResponseEntity.ok(response);
    }
}
