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

        // ✅ imageUrl이 빈 문자열이나 공백인 경우 null로 처리
        if (dto.getImageUrl() != null && !dto.getImageUrl().trim().isEmpty()) {
            post.setImageUrl(dto.getImageUrl());
        } else {
            post.setImageUrl(null);
        }

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

        // [수정] 컨트롤러에서 모든 이미지 관련 로직(업로드, 삭제, 유지)을 처리한 후,
        // 최종 결정된 이미지 URL을 DTO로부터 받아 DB에 반영합니다.
        // 이 값은 새 이미지 URL, 기존 이미지 URL, 또는 null일 수 있습니다.
        post.setImageUrl(dto.getImageUrl());

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
        Pageable pageable = PageRequest.of(page, 6);
        return postRepository.findByTitleContainingOrContentContainingOrderByIdDesc(keyword, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> findWithPagingAndSearchAndCategory(String keyword, String category, String tag, String searchType, String sort, int page) {
        keyword = (keyword == null) ? "" : keyword;
        searchType = (searchType == null) ? "titleContent" : searchType;
        sort = (sort == null) ? "latest" : sort;

        // 정렬 기준 설정
        Sort sortOrder;
        switch (sort) {
            case "popular":
                // 인기순은 별도 메서드 사용
                return
                        findWithPagingAndSearchBySortPopular(keyword, category, tag, searchType, page);
            case "rating":
                sortOrder = Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "id"));
                break;
            default: // latest
                sortOrder = Sort.by(Sort.Direction.DESC, "id");
                break;
        }

        Pageable pageable = PageRequest.of(page, 6, sortOrder);

        // [수정] 태그 필터링이 있는 경우
        if (tag != null && !tag.isEmpty()) {
            List<PostTag> postTags = postTagRepository.findByTagName(tag);
            List<Long> postIds = postTags.stream().map(pt -> pt.getPost().getId()).collect(Collectors.toList());

            if (postIds.isEmpty()) {
                // 태그에 해당하는 게시물이 없으면 빈 페이지 반환
                return Page.empty(pageable);
            }

            // 태그 + 카테고리 + 키워드 조합 처리
            if (category == null || category.isEmpty()) {
                // 태그만 또는 태그 + 키워드
                if (keyword.isEmpty()) {
                    return postRepository.findByIdIn(postIds, pageable);
                } else if ("author".equals(searchType)) {
                    // 태그 + 작성자 검색: 메모리에서 필터링
                    return filterPostsByAuthor(postIds, keyword, pageable);
                } else {
                    // 태그 + 제목/내용 검색: 메모리에서 필터링
                    return filterPostsByTitleOrContent(postIds, keyword, pageable);
                }
            } else {
                // 태그 + 카테고리 (+ 키워드 옵션)
                if (keyword.isEmpty()) {
                    return filterPostsByCategory(postIds, category, pageable);
                } else if ("author".equals(searchType)) {
                    return filterPostsByCategoryAndAuthor(postIds, category, keyword, pageable);
                } else {
                    return filterPostsByCategoryAndTitleOrContent(postIds, category, keyword, pageable);
                }
            }
        }

        // 태그 필터링이 없는 경우 (기존 로직)
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

    // 태그 필터링된 게시물 중 카테고리로 추가 필터링하는 헬퍼 메서드들
    private Page<Post> filterPostsByCategory(List<Long> postIds, String category, Pageable pageable) {
        List<Post> posts = postRepository.findAllById(postIds).stream()
                .filter(post -> category.equals(post.getCategory()))
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .collect(Collectors.toList());
        return createPageFromList(posts, pageable);
    }

    private Page<Post> filterPostsByAuthor(List<Long> postIds, String username, Pageable pageable) {
        List<Post> posts = postRepository.findAllById(postIds).stream()
                .filter(post -> post.getAuthor().getUsername().contains(username))
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .collect(Collectors.toList());
        return createPageFromList(posts, pageable);
    }

    private Page<Post> filterPostsByTitleOrContent(List<Long> postIds, String keyword, Pageable pageable) {
        List<Post> posts = postRepository.findAllById(postIds).stream()
                .filter(post -> post.getTitle().contains(keyword) || post.getContent().contains(keyword))
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .collect(Collectors.toList());
        return createPageFromList(posts, pageable);
    }

    private Page<Post> filterPostsByCategoryAndAuthor(List<Long> postIds, String category, String username, Pageable pageable) {
        List<Post> posts = postRepository.findAllById(postIds).stream()
                .filter(post -> category.equals(post.getCategory()) && post.getAuthor().getUsername().contains(username))
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .collect(Collectors.toList());
        return createPageFromList(posts, pageable);
    }

    private Page<Post> filterPostsByCategoryAndTitleOrContent(List<Long> postIds, String category, String keyword, Pageable pageable) {
        List<Post> posts = postRepository.findAllById(postIds).stream()
                .filter(post -> category.equals(post.getCategory()) &&
                        (post.getTitle().contains(keyword) || post.getContent().contains(keyword)))
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                .collect(Collectors.toList());
        return createPageFromList(posts, pageable);
    }

    private Page<Post> createPageFromList(List<Post> posts, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), posts.size());

        if (start > posts.size()) {
            return Page.empty(pageable);
        }

        return new org.springframework.data.domain.PageImpl<>(
                posts.subList(start, end),
                pageable,
                posts.size()
        );
    }

    private Page<Post> findWithPagingAndSearchBySortPopular(String keyword, String category, String tag, String searchType, int page) {
        Pageable pageable = PageRequest.of(page, 6);

        // 태그 필터링이 있는 경우
        if (tag != null && !tag.isEmpty()) {
            List<PostTag> postTags = postTagRepository.findByTagName(tag);
            List<Long> postIds = postTags.stream().map(pt -> pt.getPost().getId()).collect(Collectors.toList());

            if (postIds.isEmpty()) {
                return Page.empty(pageable);
            }

            // 태그로 필터링된 게시물들을 좋아요 수로 정렬
            List<Post> posts = postRepository.findAllById(postIds).stream()
                    .filter(post -> {
                        // 카테고리 필터 적용
                        if (category != null && !category.isEmpty() && !category.equals(post.getCategory())) {
                            return false;
                        }
                        // 키워드 필터 적용
                        if (!keyword.isEmpty()) {
                            if ("author".equals(searchType)) {
                                return post.getAuthor().getUsername().contains(keyword);
                            } else {
                                return post.getTitle().contains(keyword) || post.getContent().contains(keyword);
                            }
                        }
                        return true;
                    })
                    .sorted((p1, p2) -> {
                        long likeCount1 = getLikeCount(p1);
                        long likeCount2 = getLikeCount(p2);
                        int cmp = Long.compare(likeCount2, likeCount1);
                        if (cmp == 0) {
                            return p2.getId().compareTo(p1.getId());
                        }
                        return cmp;
                    })
                    .collect(Collectors.toList());

            return createPageFromList(posts, pageable);
        }

        // 태그 필터링이 없는 경우 (기존 로직)
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

    // 제안: 인기 태그 목록을 가져오는 서비스 메서드 추가
    @Transactional(readOnly = true)
    public List<String> getPopularTags(int limit) {
        return postTagRepository.findPopularTags(PageRequest.of(0, limit));
    }
}