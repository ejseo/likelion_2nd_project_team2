package com.example.boardpjt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 이름을 받아 Supabase Storage에서 파일 내용을 직접 스트리밍합니다.
     * @param filename 조회할 파일의 이름
     * @return 파일 데이터 스트림을 포함하는 ResponseEntity
     */
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        // GetObjectRequest를 사용하여 S3에서 객체를 가져옵니다.
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .build();

        try {
            // S3Client.getObject는 ResponseInputStream을 반환합니다.
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            GetObjectResponse s3ObjectResponse = s3Object.response();

            // InputStream을 Spring의 Resource로 래핑합니다.
            InputStreamResource resource = new InputStreamResource(s3Object);

            // HTTP 응답 헤더를 설정합니다.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(s3ObjectResponse.contentType()));
            headers.setContentLength(s3ObjectResponse.contentLength());
            // 'Content-Disposition' 헤더를 'inline'으로 설정하여 브라우저에서 바로 이미지를 표시하도록 합니다.
            // 다운로드 받게 하려면 'attachment; filename="' + filename + '"' 으로 설정합니다.
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (Exception e) {
            // 파일을 찾을 수 없거나 다른 오류 발생 시
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}