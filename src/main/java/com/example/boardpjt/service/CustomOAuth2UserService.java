package com.example.boardpjt.service;

import com.example.boardpjt.model.dto.OAuthAttributes;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        UserAccount user = saveOrUpdate(attributes);

        // Create a mutable copy of the attributes map to avoid UnsupportedOperationException
        Map<String, Object> mutableAttributes = new HashMap<>(attributes.getAttributes());
        mutableAttributes.put("username_for_jwt", user.getUsername());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                mutableAttributes, // Use the new mutable map
                attributes.getNameAttributeKey());
    }

    /**
     * Finds an existing user by username or saves a new one.
     * This prevents duplicate entry errors on subsequent logins.
     */
    private UserAccount saveOrUpdate(OAuthAttributes attributes) {
        Optional<UserAccount> userOptional = userAccountRepository.findByUsername(attributes.getUsername());
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
        return userAccountRepository.save(attributes.toEntity());
    }
}