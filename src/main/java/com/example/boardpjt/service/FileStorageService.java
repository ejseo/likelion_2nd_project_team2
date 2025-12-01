package com.example.boardpjt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    // 제안: 허용할 이미지 파일의 Content-Type 목록을 상수로 관리합니다.
    private static final List<String> ALLOWED_IMAGE_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 타입 검증
        if (!isImageFile(file)) {
            throw new IllegalArgumentException("이미지 파일 형식만 업로드할 수 있습니다. (jpeg, png, gif, bmp, webp)");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }

    /**
     * 제안: S3에서 파일을 삭제하는 메서드를 추가합니다.
     * @param fileName 삭제할 파일의 이름 (S3 객체 키)
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            log.warn("삭제할 파일 이름이 제공되지 않았습니다.");
            return;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3에서 파일 삭제 성공: {}", fileName);
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", fileName, e);
        }
    }

    @Value("${spring.cloud.aws.cloudfront.domain:}")
    private String cloudfrontDomain;

    /**
     * 파일의 URL을 반환합니다.
     * CloudFront 도메인이 설정되어 있으면 CloudFront URL을 반환하고,
     * 설정되어 있지 않으면 기본 S3 URL을 반환합니다.
     *
     * @param fileName S3에 저장된 파일명
     * @return 파일의 전체 URL
     */
    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        // CloudFront 도메인이 설정되어 있으면 CloudFront URL 사용
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
            return String.format("https://%s/%s", cloudfrontDomain, fileName);
        }

        // CloudFront 미설정 시 기본 S3 URL 반환
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, fileName);
    }


    // 이미지 파일인지 확인하는 헬퍼 메서드
    private boolean isImageFile(MultipartFile file) {
        return ALLOWED_IMAGE_CONTENT_TYPES.contains(file.getContentType());
    }
}