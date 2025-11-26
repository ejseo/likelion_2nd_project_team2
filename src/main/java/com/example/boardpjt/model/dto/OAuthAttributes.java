// src/main/java/com/example/boardpjt/model/dto/OAuthAttributes.java
package com.example.boardpjt.model.dto;

import com.example.boardpjt.model.entity.UserAccount;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String username; // "kakao_123456789" 형식의 고유 식별자
    private final String nickname;
    private final String provider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String username, String nickname, String provider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.username = username;
        this.nickname = nickname;
        this.provider = provider;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("Unsupported provider: " + registrationId);
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        // 카카오 응답에서 프로필 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                // username을 "provider_id" 형식으로 생성
                .username("kakao_" + attributes.get("id"))
                .nickname((String) profile.get("nickname"))
                .provider("kakao")
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public UserAccount toEntity() {
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(username); // 고유 식별자를 username으로 저장
        userAccount.setPassword("OAUTH_USER_PASSWORD_PLACEHOLDER");
        userAccount.setRole("ROLE_USER");
        userAccount.setProvider(provider);
        return userAccount;
    }
}