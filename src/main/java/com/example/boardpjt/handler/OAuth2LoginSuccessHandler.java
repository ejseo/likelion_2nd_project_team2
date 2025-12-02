// src/main/java/com/example/boardpjt/handler/OAuth2LoginSuccessHandler.java
package com.example.boardpjt.handler;

import com.example.boardpjt.model.entity.RefreshToken;
import com.example.boardpjt.model.repository.RefreshTokenRepository;
import com.example.boardpjt.util.CookieUtil;
import com.example.boardpjt.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            log.info("OAuth2 login success handler started");
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Retrieve the username from the custom attribute we added
            String username = (String) oAuth2User.getAttribute("username_for_jwt");
            log.info("Retrieved username: {}", username);

            if (username == null) {
                log.error("Username is null from OAuth2User attributes");
                response.sendRedirect("/auth/login?error=oauth_failed");
                return;
            }

            // Generate and set tokens
            String accessToken = jwtUtil.generateToken(username, "ROLE_USER", false);
            CookieUtil.createCookie(response, "access_token", accessToken, 60 * 60);
            log.info("Access token created");

            String refreshToken = jwtUtil.generateToken(username, "ROLE_USER", true);
            refreshTokenRepository.save(new RefreshToken(username, refreshToken));
            CookieUtil.createCookie(response, "refresh_token", refreshToken, 60 * 60 * 24 * 7);
            log.info("Refresh token created");

            log.info("Redirecting to index page");
            response.sendRedirect("/");
        } catch (Exception e) {
            log.error("Error in OAuth2 login success handler", e);
            response.sendRedirect("/auth/login?error=oauth_failed");
        }
    }
}