package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.storage.CloudinaryStorageService;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/uploads")
public class AdminUploadController {

    private final CloudinaryStorageService storageService;

    public AdminUploadController(CloudinaryStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CloudinaryStorageService.UploadResult>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "portfolio/resumes") String folder)
            throws IOException {
        CloudinaryStorageService.UploadResult result = storageService.upload(file, folder);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
