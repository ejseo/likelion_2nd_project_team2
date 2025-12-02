package com.example.boardpjt.service;

import com.example.boardpjt.model.entity.Bookmark;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.BookmarkRepository;
import com.example.boardpjt.model.repository.PostRepository;
import com.example.boardpjt.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserAccountRepository userAccountRepository;
    private final PostRepository postRepository;

    @Transactional
    public void toggleBookmark(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물 없음"));
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAccountAndPost(userAccount, post);

        if (bookmark.isPresent()) {
            bookmarkRepository.delete(bookmark.get());
        } else {
            bookmarkRepository.save(new Bookmark(userAccount, post));
        }
    }

    @Transactional(readOnly = true)
    public List<Bookmark> findMyBookmarks(String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return bookmarkRepository.findByUserAccount(userAccount);
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, String username) {
        // 사용자가 존재하지 않을 경우 false를 반환하여 예외를 방지
        Optional<UserAccount> userAccountOpt = userAccountRepository.findByUsername(username);
        if (userAccountOpt.isEmpty()) {
            return false;
        }
        
        // 게시물이 존재하지 않을 경우 false를 반환
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return false;
        }

        return bookmarkRepository.findByUserAccountAndPost(userAccountOpt.get(), postOpt.get()).isPresent();
    }
}
