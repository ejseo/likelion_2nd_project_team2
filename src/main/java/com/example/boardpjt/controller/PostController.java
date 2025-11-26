package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.service.FileStorageService;
import com.example.boardpjt.service.FollowService;
import com.example.boardpjt.service.PostService;
import com.example.boardpjt.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserAccountService userAccountService;
    private final FileStorageService fileStorageService;
    private final FollowService followService;

    @GetMapping
    public String list(Model model,
                      @RequestParam(defaultValue = "1") int page,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String category) {
        keyword = (keyword == null) ? "" : keyword;
        Page<Post> postPage = postService.findWithPagingAndSearchAndCategory(keyword, category, page - 1);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("posts",
                // [수정] DTO 변환 시 category, rating, tags 추가
                postPage.getContent().stream().map(p -> new PostDTO.Response(
                        p.getId(), p.getTitle(), p.getContent(),
                        p.getAuthor().getUsername(), p.getCreatedAt(), p.getImageUrl(),
                        p.getCategory(), p.getRating(), p.getTags()
                )).toList()
        );
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category != null ? category : "");
        return "post/list";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("post") PostDTO.Request dto,
                         BindingResult bindingResult,
                         @RequestParam("file") MultipartFile file,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "post/form";
        }

        try {
            String savedFileName = fileStorageService.uploadFile(file);
            dto.setImageUrl(savedFileName);
            dto.setUsername(authentication.getName());
            postService.createPost(dto);

        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "파일 업로드에 실패했습니다: " + e.getMessage());
            return "redirect:/posts/new";
        }

        return "redirect:/posts";
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
            return "post/edit";
        }

        try {
            // 파일이 새로 첨부된 경우에만 업로드 및 URL 변경
            if (file != null && !file.isEmpty()) {
                String savedFileName = fileStorageService.uploadFile(file);
                dto.setImageUrl(savedFileName);
            }

            dto.setUsername(authentication.getName());
            postService.updatePost(id, dto);

        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "파일 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/posts/" + id + "/edit";
        }

        return "redirect:/posts/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);

        // 팔로우 여부 체크
        if (authentication != null) {
            boolean isFollowing = followService.isFollowing(authentication.getName(), post.getAuthor().getId());
            model.addAttribute("followCheck", isFollowing);
        } else {
            model.addAttribute("followCheck", false);
        }

        return "post/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("post", new PostDTO.Request());
        return "post/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.findById(id);
        PostDTO.Request dto = new PostDTO.Request();
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        // [추가] 기존 카테고리, 평점, 태그 값 로드
        dto.setCategory(post.getCategory());
        dto.setRating(post.getRating());
        dto.setTags(post.getTags());
        model.addAttribute("post", dto);
        model.addAttribute("postId", id);
        // [추가] 기존 이미지 URL도 전달 (수정 폼에서 미리보기용)
        model.addAttribute("existingImageUrl", post.getImageUrl());
        return "post/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.deleteById(id);
        return "redirect:/posts";
    }
}