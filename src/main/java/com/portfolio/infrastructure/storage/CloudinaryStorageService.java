package com.portfolio.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;
    private final boolean isConfigured;

    public record UploadResult(
            String fileUrl, String publicId, String originalFileName, String format, long fileSize) {}

    public CloudinaryStorageService(
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        if (cloudName != null
                && !cloudName.isBlank()
                && apiKey != null
                && !apiKey.isBlank()
                && apiSecret != null
                && !apiSecret.isBlank()) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName.trim(),
                    "api_key", apiKey.trim(),
                    "api_secret", apiSecret.trim(),
                    "secure", true));
            this.isConfigured = true;
            log.info("CloudinaryStorageService initialized successfully with cloud_name: {}", cloudName);
        } else {
            this.cloudinary = null;
            this.isConfigured = false;
            log.warn(
                    "Cloudinary credentials are not fully configured. File uploads to Cloudinary will be simulated or fail until CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET are provided.");
        }
    }

    public UploadResult upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_file";
        String targetFolder = folder != null && !folder.isBlank() ? folder : "portfolio/resumes";

        if (!isConfigured || cloudinary == null) {
            log.warn("Cloudinary is not configured. Returning fallback mock URL for {}", originalName);
            return new UploadResult(
                    "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    "sample_" + System.currentTimeMillis(),
                    originalName,
                    "pdf",
                    file.getSize());
        }

        try {
            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "folder", targetFolder, "use_filename", true, "unique_filename", true, "resource_type", "auto");

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            String format = (String) result.get("format");
            Number bytes = (Number) result.get("bytes");
            long size = bytes != null ? bytes.longValue() : file.getSize();

            log.info("File uploaded successfully to Cloudinary: {} (size: {} bytes)", secureUrl, size);
            return new UploadResult(secureUrl, publicId, originalName, format, size);
        } catch (Exception e) {
            log.error("Cloudinary upload failed for file {}: {}", originalName, e.getMessage(), e);
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }
}
