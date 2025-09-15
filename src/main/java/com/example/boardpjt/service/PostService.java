package com.example.boardpjt.service;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.PostRepository;
import com.example.boardpjt.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public Post createPost(PostDTO.Request dto) {
        UserAccount userAccount = userAccountRepository
                .findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Post post = new Post();
        post.setAuthor(userAccount);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        // [추가] 이미지 URL 설정
        post.setImageUrl(dto.getImageUrl());
        return postRepository.save(post);
    }

    @Transactional
    public void updatePost(Long id, PostDTO.Request dto) {
        Post post = findById(id);
        if (!post.getAuthor().getUsername().equals(dto.getUsername())) {
            throw new SecurityException("작성자만 수정 가능");
        }
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        // [수정] 새 이미지 URL이 있을 경우에만 업데이트
        if (dto.getImageUrl() != null) {
            post.setImageUrl(dto.getImageUrl());
        }
        postRepository.save(post);
    }

    // ... 기존 find, delete 등의 메서드는 그대로 유지 ...
    @Transactional(readOnly = true)
    public Page<Post> findWithPagingAndSearch(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return postRepository.findByTitleContainingOrContentContainingOrderByIdDesc(keyword, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물 없음"));
    }

    @Transactional
    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }
}
