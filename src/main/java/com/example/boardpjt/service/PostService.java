package com.example.boardpjt.service;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostLike;
import com.example.boardpjt.model.entity.PostTag;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.PostLikeRepository;
import com.example.boardpjt.model.repository.PostRepository;
import com.example.boardpjt.model.repository.PostTagRepository;
import com.example.boardpjt.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostTagRepository postTagRepository;

    @Transactional
    public Post createPost(PostDTO.Request dto, String username) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        Post post = new Post();
        post.setAuthor(userAccount);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setImageUrl(dto.getImageUrl());
        post.setCategory(dto.getCategory());
        post.setRating(dto.getRating());

        if (dto.getTags() != null) {
            List<PostTag> postTags = dto.getTags().stream()
                    .map(tagName -> new PostTag(post, tagName))
                    .collect(Collectors.toList());
            post.getPostTags().addAll(postTags);
        }

        return postRepository.save(post);
    }

    @Transactional
    public void updatePost(Long id, PostDTO.Request dto, String username) {
        Post post = findById(id);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("작성자만 수정 가능");
        }
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        if (dto.getImageUrl() != null) {
            post.setImageUrl(dto.getImageUrl());
        }
        post.setCategory(dto.getCategory());
        post.setRating(dto.getRating());

        post.getPostTags().clear();
        if (dto.getTags() != null) {
            List<PostTag> postTags = dto.getTags().stream()
                    .map(tagName -> new PostTag(post, tagName))
                    .collect(Collectors.toList());
            post.getPostTags().addAll(postTags);
        }

        postRepository.save(post);
    }

    @Transactional
    public long toggleLike(Long postId, String username) {
        Post post = findById(postId);
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Optional<PostLike> postLike = postLikeRepository.findByUserAccountAndPost(userAccount, post);

        if (postLike.isPresent()) {
            postLikeRepository.delete(postLike.get());
        } else {
            postLikeRepository.save(new PostLike(userAccount, post));
        }
        return postLikeRepository.countByPost(post);
    }

    @Transactional(readOnly = true)
    public Page<Post> findByTag(String tagName, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        List<PostTag> postTags = postTagRepository.findByTagName(tagName);
        List<Long> postIds = postTags.stream().map(pt -> pt.getPost().getId()).collect(Collectors.toList());
        return postRepository.findByIdIn(postIds, pageable);
    }
    
    @Transactional(readOnly = true)
    public long getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, String username) {
        Optional<UserAccount> userAccountOpt = userAccountRepository.findByUsername(username);
        if (userAccountOpt.isEmpty()) {
            return false;
        }
        
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            return false;
        }

        return postLikeRepository.findByUserAccountAndPost(userAccountOpt.get(), postOpt.get()).isPresent();
    }

    @Transactional(readOnly = true)
    public Page<Post> findWithPagingAndSearch(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return postRepository.findByTitleContainingOrContentContainingOrderByIdDesc(keyword, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> findWithPagingAndSearchAndCategory(String keyword, String category, String searchType, String sort, int page) {
        keyword = (keyword == null) ? "" : keyword;
        searchType = (searchType == null) ? "titleContent" : searchType;
        sort = (sort == null) ? "latest" : sort;

        // 정렬 기준 설정
        Sort sortOrder;
        switch (sort) {
            case "popular":
                // 인기순은 별도 메서드 사용
                return findWithPagingAndSearchBySortPopular(keyword, category, searchType, page);
            case "rating":
                sortOrder = Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "id"));
                break;
            default: // latest
                sortOrder = Sort.by(Sort.Direction.DESC, "id");
                break;
        }

        Pageable pageable = PageRequest.of(page, 5, sortOrder);

        if (category == null || category.isEmpty()) {
            // 카테고리 없이 검색
            if (keyword.isEmpty()) {
                return postRepository.findAll(pageable);
            } else if ("author".equals(searchType)) {
                return postRepository.findByAuthor_UsernameContaining(keyword, pageable);
            } else {
                return postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
            }
        } else {
            // 카테고리와 함께 검색
            if (keyword.isEmpty()) {
                return postRepository.findByCategory(category, pageable);
            } else if ("author".equals(searchType)) {
                return postRepository.findByCategoryAndAuthor_UsernameContaining(category, keyword, pageable);
            } else {
                return postRepository.findByCategoryAndTitleContainingOrCategoryAndContentContaining(
                        category, keyword, category, keyword, pageable);
            }
        }
    }

    @Transactional(readOnly = true)
    private Page<Post> findWithPagingAndSearchBySortPopular(String keyword, String category, String searchType, int page) {
        Pageable pageable = PageRequest.of(page, 5);

        if (category == null || category.isEmpty()) {
            // 카테고리 없이 검색
            if (keyword.isEmpty()) {
                return postRepository.findAllOrderByLikeCount(pageable);
            } else if ("author".equals(searchType)) {
                return postRepository.findByAuthor_UsernameContainingOrderByLikeCount(keyword, pageable);
            } else {
                return postRepository.findByTitleContainingOrContentContainingOrderByLikeCount(keyword, keyword, pageable);
            }
        } else {
            // 카테고리와 함께 검색
            if (keyword.isEmpty()) {
                return postRepository.findByCategoryOrderByLikeCount(category, pageable);
            } else if ("author".equals(searchType)) {
                return postRepository.findByCategoryAndAuthor_UsernameContainingOrderByLikeCount(category, keyword, pageable);
            } else {
                return postRepository.findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByLikeCount(
                        category, keyword, category, keyword, pageable);
            }
        }
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findByIdWithTags(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물 없음"));
    }

    @Transactional
    public void deleteById(Long id, String username) {
        Post post = findById(id);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("작성자만 삭제 가능");
        }
        postRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Post> findByUsername(String username, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return postRepository.findByAuthor_UsernameOrderByIdDesc(username, pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> findRecentPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Post> page = postRepository.findByTitleContainingOrContentContainingOrderByIdDesc("", "", pageable);
        return page.getContent();
    }
}
