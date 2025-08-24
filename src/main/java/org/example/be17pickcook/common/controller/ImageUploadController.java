package org.example.be17pickcook.common.controller;

import lombok.RequiredArgsConstructor;
import org.example.be17pickcook.common.BaseResponse;
import org.example.be17pickcook.common.service.PresignedUploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

@RestController
@RequestMapping("/image-upload")
@RequiredArgsConstructor
public class ImageUploadController {
    private final PresignedUploadService presignedUploadService;

    @PostMapping
    public BaseResponse<String> upload(@RequestParam MultipartFile file) throws SQLException, IOException {
        String url = presignedUploadService.upload(file);
        System.out.println("url:"+url);
        return BaseResponse.success(url);
    }
}
