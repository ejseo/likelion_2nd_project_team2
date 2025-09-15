package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PostDTO;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.service.FileStorageService;
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
    // [추가] FileStorageService 주입
    private final FileStorageService fileStorageService;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue = "1") int page, @RequestParam(required = false) String keyword) {
        keyword = (keyword == null) ? "" : keyword;
        Page<Post> postPage = postService.findWithPagingAndSearch(keyword, page - 1);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("posts",
                // [수정] DTO 변환 시 imageUrl 추가
                postPage.getContent().stream().map(p -> new PostDTO.Response(
                        p.getId(), p.getTitle(), p.getContent(),
                        p.getAuthor().getUsername(), p.getCreatedAt(), p.getImageUrl()
                )).toList()
        );
        model.addAttribute("keyword", keyword);
        return "post/list";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("post") PostDTO.Request dto,
                         BindingResult bindingResult,
                         @RequestParam("file") MultipartFile file,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) { // RedirectAttributes 추가
        if (bindingResult.hasErrors()) {
            return "post/form";
        }

        try {
            String savedFileName = fileStorageService.uploadFile(file);
            dto.setImageUrl(savedFileName);
            dto.setUsername(authentication.getName());
            postService.createPost(dto);

        } catch (IOException | IllegalArgumentException e) {
            // [추가] 파일 업로드 중 오류 발생 시
            e.printStackTrace(); // 서버 로그에 오류 기록
            // 사용자에게 보여줄 오류 메시지를 Flash Attribute로 추가
            redirectAttributes.addFlashAttribute("error", "파일 업로드에 실패했습니다: " + e.getMessage());
            return "redirect:/posts/new"; // 작성 폼으로 다시 리다이렉트
        }

        return "redirect:/posts";
    }

    // edit 메서드도 동일하게 '파일 이름'을 저장하도록 유지합니다.
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("post") PostDTO.Request dto,
                       BindingResult bindingResult,
                       @RequestParam("file") MultipartFile file,
                       Authentication authentication,
                       Model model,
                       RedirectAttributes redirectAttributes) { // RedirectAttributes 추가
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
            // [추가] 파일 업로드 중 오류 발생 시 처리
            e.printStackTrace(); // 서버 로그에 오류 기록
            // 사용자에게 보여줄 오류 메시지를 Flash Attribute로 추가
            redirectAttributes.addFlashAttribute("error", "파일 수정에 실패했습니다: " + e.getMessage());
            return "redirect:/posts/" + id + "/edit"; // 수정 폼으로 다시 리다이렉트
        }

        return "redirect:/posts/" + id;
    }


    // ... detail, form, delete 등 나머지 메서드는 기존과 동일하게 유지 ...
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
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
        model.addAttribute("post", dto);
        model.addAttribute("postId", id);
        return "post/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.deleteById(id);
        return "redirect:/posts";
    }
}