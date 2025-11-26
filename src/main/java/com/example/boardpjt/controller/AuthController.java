package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.UserRegisterDTO;
import com.example.boardpjt.model.entity.RefreshToken;
import com.example.boardpjt.model.repository.RefreshTokenRepository;
import com.example.boardpjt.service.UserAccountService;
import com.example.boardpjt.util.CookieUtil;
import com.example.boardpjt.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userRegisterDTO", new UserRegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserRegisterDTO userRegisterDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userAccountService.register(userRegisterDTO);
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));

            String accessToken = jwtUtil.generateToken(
                    username,
                    authentication.getAuthorities().toString(),
                    false
            );
            CookieUtil.createCookie(response, "access_token", accessToken, 60 * 60);

            String refreshToken = jwtUtil.generateToken(
                    username,
                    authentication.getAuthorities().toString(),
                    true
            );
            refreshTokenRepository.save(new RefreshToken(username, refreshToken));
            CookieUtil.createCookie(response, "refresh_token", refreshToken, 60 * 60 * 24 * 7);

            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "redirect:/auth/login";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpServletResponse response, Authentication authentication) {
        CookieUtil.deleteCookie(response, "access_token");
        CookieUtil.deleteCookie(response, "refresh_token");
        if (authentication != null) {
            refreshTokenRepository.deleteById(authentication.getName());
        }
        return "redirect:/";
    }
}