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
                      @RequestParam(defaultValue = "titleContent") String searchType,
                      @RequestParam(defaultValue = "latest") String sort) {
        keyword = (keyword == null) ? "" : keyword;
        Page<Post> postPage = postService.findWithPagingAndSearchAndCategory(keyword, category, searchType, sort, page - 1);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("posts",
                postPage.getContent().stream()
                        .map(post -> PostDTO.Response.from(post, postService.getLikeCount(post)))
                        .collect(Collectors.toList())
        );
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("searchType", searchType);
        model.addAttribute("sort", sort);
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
            if (file != null && !file.isEmpty()) {
                String savedFileName = fileStorageService.uploadFile(file);
                dto.setImageUrl(savedFileName);
            }
            postService.createPost(dto, authentication.getName());

        } catch (IOException | IllegalArgumentException e) {
            log.error("File upload failed", e);
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
            if (file != null && !file.isEmpty()) {
                String savedFileName = fileStorageService.uploadFile(file);
                dto.setImageUrl(savedFileName);
            }
            postService.updatePost(id, dto, authentication.getName());

        } catch (IOException | IllegalArgumentException | SecurityException e) {
            log.error("Post update failed", e);
            redirectAttributes.addFlashAttribute("error", "게시글 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/posts/" + id + "/edit";
        }

        return "redirect:/posts/" + id;
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
