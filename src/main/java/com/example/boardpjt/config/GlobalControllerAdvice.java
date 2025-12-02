package com.example.boardpjt.config;

import com.example.boardpjt.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 모든 컨트롤러에 공통으로 적용되는 설정을 정의하는 클래스
 * 모든 뷰 템플릿에서 FileStorageService를 사용할 수 있도록 주입
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final FileStorageService fileStorageService;

    /**
     * 모든 컨트롤러의 Model에 fileStorageService를 자동으로 추가
     * Thymeleaf 템플릿에서 ${fileStorageService.getFileUrl(fileName)} 형태로 사용 가능
     */
    @ModelAttribute("fileStorageService")
    public FileStorageService fileStorageService() {
        return fileStorageService;
    }
}
