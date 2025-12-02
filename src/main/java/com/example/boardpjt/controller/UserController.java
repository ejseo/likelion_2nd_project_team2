package com.example.boardpjt.controller;

import com.example.boardpjt.model.dto.PasswordChangeDTO;
import com.example.boardpjt.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserAccountService userAccountService;

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeDTO passwordChangeDTO,
                                 BindingResult bindingResult,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "user/change-password";
        }

        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getNewPasswordConfirm())) {
            bindingResult.rejectValue("newPasswordConfirm", "passwordInCorrect", "새 비밀번호가 일치하지 않습니다.");
            return "user/change-password";
        }

        try {
            userAccountService.changePassword(authentication.getName(), passwordChangeDTO.getCurrentPassword(), passwordChangeDTO.getNewPassword());
            redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/my-page";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("currentPassword", "passwordInCorrect", e.getMessage());
            return "user/change-password";
        }
    }

    @GetMapping("/withdraw")
    public String withdrawForm() {
        return "user/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String password,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            userAccountService.withdraw(authentication.getName(), password);
            // 로그아웃 처리도 함께 필요
            return "redirect:/auth/logout";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/withdraw";
        }
    }
}