package com.example.boardpjt.controller;

import com.example.boardpjt.model.entity.Bookmark;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostLike;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.BookmarkRepository;
import com.example.boardpjt.model.repository.PostLikeRepository;
import com.example.boardpjt.model.repository.UserAccountRepository;
import com.example.boardpjt.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 메인 페이지 및 사용자 페이지를 담당하는 컨트롤러
 * 애플리케이션의 홈페이지와 인증된 사용자의 마이페이지를 제공
 */
@Controller // Spring MVC 컨트롤러로 등록 (ViewResolver를 통해 뷰 이름을 실제 뷰로 변환)
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;
    private final UserAccountRepository userAccountRepository;
    private final PostLikeRepository postLikeRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 애플리케이션의 홈페이지(메인 페이지)를 보여주는 메서드
     * 루트 경로("/") 접근 시 호출되며, 모든 사용자가 접근 가능
     *
     * @param model Spring MVC의 Model 객체 - 뷰에 데이터를 전달하기 위한 컨테이너
     * @return String - 렌더링할 템플릿 파일명 (templates/index.html)
     */
    @GetMapping // 경로를 명시하지 않으면 기본적으로 루트 경로("/")에 매핑됨
    public String index(Model model) {
        // ViewResolver가 "index"를 templates/index.html로 변환하여 렌더링
        // 일반적으로 애플리케이션 소개, 로그인/회원가입 링크 등이 포함된 메인 페이지

        // 최근 게시글 3개 조회
        model.addAttribute("recentPosts", postService.findRecentPosts(3));

        return "index";
    }

    /**
     * 소개 페이지를 보여주는 메서드
     * Travelog 서비스 소개 페이지 제공
     *
     * @return String - 렌더링할 템플릿 파일명 (templates/about.html)
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    /**
     * 인증된 사용자의 마이페이지를 보여주는 메서드
     * 로그인한 사용자만 접근 가능하며, 사용자 정보를 템플릿에 전달
     *
     * @param model Spring MVC의 Model 객체 - 뷰에 데이터를 전달하기 위한 컨테이너
     * @param authentication Spring Security의 Authentication 객체 - 현재 인증된 사용자 정보
     * @param page 페이지 번호 (기본값: 0)
     * @return String - 렌더링할 템플릿 파일명 (templates/my-page.html)
     */
    @GetMapping("/my-page") // GET /my-page 요청 처리
    public String myPage(Model model,
                        Authentication authentication,
                        @RequestParam(defaultValue = "0") int page) {

        // === 인증 상태 확인 및 사용자 정보 전달 ===
        if (authentication != null) {
            String username = authentication.getName();

            // 사용자 정보 조회
            UserAccount user = userAccountRepository.findByUsername(username)
                    .orElse(null);

            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("username", username);
                model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());

                // 사용자가 작성한 게시글 조회
                Page<Post> userPosts = postService.findByUsername(username, page);
                model.addAttribute("posts", userPosts.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", userPosts.getTotalPages());
                model.addAttribute("totalPosts", userPosts.getTotalElements());

                // 좋아요한 글 조회
                List<PostLike> likedPosts = postLikeRepository.findByUserAccount(user);
                List<Post> likedPostsList = likedPosts.stream()
                        .map(PostLike::getPost)
                        .collect(Collectors.toList());
                model.addAttribute("likedPosts", likedPostsList);

                // 북마크한 글 조회
                List<Bookmark> bookmarks = bookmarkRepository.findByUserAccount(user);
                List<Post> bookmarkedPosts = bookmarks.stream()
                        .map(Bookmark::getPost)
                        .collect(Collectors.toList());
                model.addAttribute("bookmarkedPosts", bookmarkedPosts);
            }
        }

        return "my-page";
    }
}