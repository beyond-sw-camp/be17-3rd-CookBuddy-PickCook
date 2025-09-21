package org.example.be17pickcook.common.service;

import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class S3UploadService implements UploadService{
    @Value("${spring.cloud.aws.s3.bucket}")
    private String s3BucketName;

    private final S3Operations s3Operations;
    @Override
    public String upload(MultipartFile file) throws SQLException, IOException {
        String dirPath = FileUploadUtil.makeUploadPath();

        S3Resource s3Resource = s3Operations.upload(s3BucketName, dirPath + file.getOriginalFilename(), file.getInputStream());
        return s3Resource.getURL().toString();
    }

    // key로 삭제
    public void deleteByKey(String key) {
        if (key == null || key.isEmpty()) return;
        s3Operations.deleteObject(s3BucketName, key);
    }

    // URL로 삭제 (URL에서 bucket과 key 추출)
    public void deleteByUrl(String s3Url) {
        if (s3Url == null || s3Url.isEmpty()) return;

        try {
            URI uri = new URI(s3Url);
            // URL 경로에서 key 추출
            String key = uri.getPath();
            if (key.startsWith("/")) key = key.substring(1); // 맨 앞 / 제거
            s3Operations.deleteObject(s3BucketName, key);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("유효하지 않은 S3 URL입니다: " + s3Url, e);
        }
    }

}
