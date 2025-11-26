package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Bookmark;
import com.example.boardpjt.service.BookmarkService;
import com.example.boardpjt.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkApiController {

    private final BookmarkService bookmarkService;
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDTO.Response>> getMyBookmarks(Authentication authentication) {
        List<Bookmark> bookmarks = bookmarkService.findMyBookmarks(authentication.getName());
        List<PostDTO.Response> response = bookmarks.stream()
                .map(bookmark -> PostDTO.Response.from(bookmark.getPost(), postService.getLikeCount(bookmark.getPost())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
