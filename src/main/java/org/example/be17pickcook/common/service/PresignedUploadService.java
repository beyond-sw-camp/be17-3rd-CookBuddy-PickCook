package org.example.be17pickcook.common.service;

import jakarta.annotation.PostConstruct;
import org.example.be17pickcook.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

@Service
public class PresignedUploadService implements UploadService {

    @Value("${ACCESS_KEY}")
    private String accessKey;

    @Value("${SECRET_KEY}")
    private String secretKey;

    @Value("${MY_BUCKET}")
    private String bucketName;

    @Value("${MY_REGION}")
    private String region;

    private S3Presigner s3Presigner;

    // 초기화
    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    @Override
    public String upload(MultipartFile file) throws SQLException, IOException {
        String dirPath = FileUploadUtil.makeUploadPath();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(dirPath)
                .contentType(file.getContentType())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                b -> b.putObjectRequest(putObjectRequest)
                        .signatureDuration(Duration.ofMinutes(10)) // URL 만료 시간
        );

        // 생성된 URL 반환 (클라이언트가 직접 S3에 PUT 요청 가능)
        return presignedRequest.url().toString();
    }
}
