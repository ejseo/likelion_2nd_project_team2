package com.example.boardpjt.service;

import com.example.boardpjt.model.dto.UserRegisterDTO;
import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserAccount register(UserRegisterDTO dto) {
        if (userAccountRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + dto.getUsername());
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(dto.getUsername());
        userAccount.setPassword(passwordEncoder.encode(dto.getPassword()));
        userAccount.setRole("ROLE_USER");
        return userAccountRepository.save(userAccount);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, userAccount.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        userAccount.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(userAccount);
    }

    @Transactional
    public void withdraw(String username, String password) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, userAccount.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        // 연관된 데이터 삭제 로직 추가 (게시물, 댓글 등)
        userAccountRepository.delete(userAccount);
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAllUsers() {
        return userAccountRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long id) {
        userAccountRepository.deleteById(id);
    }

    public UserAccount findByUsername(String name) {
        return userAccountRepository.findByUsername(name).orElseThrow();
    }
}