package org.example.be17pickcook.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

public interface UploadService {
    public String upload(MultipartFile file) throws SQLException, IOException;
}
