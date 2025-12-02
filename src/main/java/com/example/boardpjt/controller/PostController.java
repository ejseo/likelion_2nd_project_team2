package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostTag;
import com.example.boardpjt.service.BookmarkService;
import com.example.boardpjt.service.FileStorageService;
import com.example.boardpjt.service.FollowService;
import com.example.boardpjt.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
@Slf4j
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final FollowService followService;
    private final BookmarkService bookmarkService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String tag, // [수정] 태그 파라미터 추가
                       @RequestParam(defaultValue = "titleContent") String searchType,
                       @RequestParam(defaultValue = "latest") String sort) {
        keyword = (keyword == null) ? "" : keyword;
        Page<Post> postPage = postService.findWithPagingAndSearchAndCategory(keyword, category, tag, searchType, sort, page - 1); // [수정] 태그 파라미터 전달
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("posts",
                postPage.getContent().stream()
                        .map(post -> PostDTO.Response.from(post, postService.getLikeCount(post), fileStorageService))
                        .collect(Collectors.toList())
        );
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("selectedTag", tag != null ? tag : ""); // [수정] 선택된 태그를 모델에 추가
        model.addAttribute("searchType", searchType);
        model.addAttribute("sort", sort);
        model.addAttribute("totalElements", postPage.getTotalElements());

        // 제안: 인기 태그 목록을 조회하여 모델에 추가 (상위 5개)
        model.addAttribute("popularTags", postService.getPopularTags(5));

        return "post/list";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("post") PostDTO.Request dto,
                         BindingResult bindingResult,
                         @RequestParam("file") MultipartFile file,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes,
                         Model model) {  // ✅ Model 추가

        if (bindingResult.hasErrors()) {
            // ✅ 에러 발생 시 폼으로 돌아갈 때 깨끗한 상태로
            model.addAttribute("post", dto);
            return "post/form";
        }

        try {
            // ✅ 파일이 있을 때만 업로드 처리
            if (file != null && !file.isEmpty()) {
                String savedFileName = fileStorageService.uploadFile(file);
                dto.setImageUrl(savedFileName);
            } else {
                // ✅ 파일이 없으면 imageUrl을 명시적으로 null로 설정
                dto.setImageUrl(null);
            }

            postService.createPost(dto, authentication.getName());
            return "redirect:/posts";

        } catch (IllegalArgumentException e) {
            // ✅ 이미지 업로드 실패 시 - 폼으로 돌아가면서 데이터는 유지하되 imageUrl은 제거
            log.error("File upload failed", e);
            dto.setImageUrl(null);  // ⭐ 중요: 실패한 imageUrl 초기화
            model.addAttribute("post", dto);
            model.addAttribute("error", "파일 업로드에 실패했습니다: " + e.getMessage());
            return "post/form";  // redirect 대신 forward로 변경

        } catch (IOException e) {
            log.error("File IO error", e);
            dto.setImageUrl(null);
            model.addAttribute("post", dto);
            model.addAttribute("error", "파일 처리 중 오류가 발생했습니다.");
            return "post/form";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("post") PostDTO.Request dto,
                       BindingResult bindingResult,
                       @RequestParam("file") MultipartFile file,
                       Authentication authentication,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("post", dto);
            return "post/edit";
        }

        try {
            Post existingPost = postService.findById(id);
            String existingImageUrl = existingPost.getImageUrl();

            // 1. 새 파일이 업로드된 경우
            if (file != null && !file.isEmpty()) {
                // 기존 이미지가 있으면 삭제
                if (existingImageUrl != null) {
                    fileStorageService.deleteFile(existingImageUrl);
                }
                // 새 파일 업로드 후 DTO에 URL 설정
                String savedFileName = fileStorageService.uploadFile(file);
                dto.setImageUrl(savedFileName);
            }
            // 2. 새 파일이 없고, 이미지 삭제가 요청된 경우
            else if (dto.isDeleteImage()) {
                if (existingImageUrl != null) {
                    fileStorageService.deleteFile(existingImageUrl);
                }
                dto.setImageUrl(null); // DB에 null로 업데이트하도록 설정
            }
            // 3. 새 파일도 없고, 삭제 요청도 없는 경우 (기존 이미지 유지)
            else {
                // [핵심 수정] edit.html의 hidden input으로 받은 기존 이미지 URL을 DTO에 설정합니다.
                dto.setImageUrl(existingImageUrl);
            }

            postService.updatePost(id, dto, authentication.getName());
            return "redirect:/posts/" + id;

        } catch (IllegalArgumentException e) {
            log.error("File upload failed", e);
            model.addAttribute("postId", id);
            model.addAttribute("post", dto);
            model.addAttribute("error", "파일 업로드에 실패했습니다: " + e.getMessage());
            return "post/edit";

        } catch (IOException e) {
            log.error("File IO error", e);
            model.addAttribute("postId", id);
            model.addAttribute("post", dto);
            model.addAttribute("error", "파일 처리 중 오류가 발생했습니다.");
            return "post/edit";

        } catch (SecurityException e) {
            log.error("Post update permission denied", e);
            redirectAttributes.addFlashAttribute("error", "게시글을 수정할 권한이 없습니다.");
            return "redirect:/posts/" + id;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);

        long likeCount = postService.getLikeCount(post);
        model.addAttribute("likeCount", likeCount);

        boolean isBookmarked = false;
        boolean isLiked = false;
        if (authentication != null) {
            String username = authentication.getName();
            isBookmarked = bookmarkService.isBookmarked(id, username);
            isLiked = postService.isLiked(id, username);
            boolean isFollowing = followService.isFollowing(username, post.getAuthor().getId());
            model.addAttribute("followCheck", isFollowing);
        } else {
            model.addAttribute("followCheck", false);
        }
        model.addAttribute("isBookmarked", isBookmarked);
        model.addAttribute("isLiked", isLiked);

        return "post/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        // ✅ 항상 새로운 깨끗한 DTO 객체 생성
        model.addAttribute("post", new PostDTO.Request());
        return "post/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        PostDTO.Request dto = new PostDTO.Request();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCategory(post.getCategory());
        dto.setRating(post.getRating());
        dto.setTags(post.getPostTags().stream().map(PostTag::getTagName).collect(Collectors.toList()));
        model.addAttribute("post", dto);
        model.addAttribute("postId", id);
        model.addAttribute("existingImageUrl", post.getImageUrl());
        return "post/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            postService.deleteById(id, authentication.getName());
        } catch (SecurityException e) {
            log.warn("Post delete permission denied", e);
            redirectAttributes.addFlashAttribute("error", "게시글을 삭제할 권한이 없습니다.");
            return "redirect:/posts/" + id;
        }
        return "redirect:/posts";
    }
}
