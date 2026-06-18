package com.twilight.campus.controller;

import com.twilight.campus.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final long MAX_CONTENT_IMAGE_SIZE = 20L * 1024 * 1024;
    private static final long MAX_CONTENT_VIDEO_SIZE = 2L * 1024 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> ALLOWED_MEDIA_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    @Value("${campus.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择图片文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return Result.error(400, "只支持上传图片文件");
        }

        String extension = getExtension(contentType);
        String fileName = UUID.randomUUID() + extension;
        Path directory = Paths.get(uploadPath).toAbsolutePath().normalize().resolve("profile");
        Files.createDirectories(directory);

        Path target = directory.resolve(fileName).normalize();
        file.transferTo(target);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return Result.success(baseUrl + "/uploads/profile/" + fileName);
    }

    @PostMapping("/media")
    public Result<String> uploadMedia(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MEDIA_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return Result.error(400, "只支持上传图片或视频文件");
        }

        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (normalizedContentType.startsWith("image/") && file.getSize() > MAX_CONTENT_IMAGE_SIZE) {
            return Result.error(400, "每张图片不能超过20MB");
        }
        if (normalizedContentType.startsWith("video/") && file.getSize() > MAX_CONTENT_VIDEO_SIZE) {
            return Result.error(400, "每个视频不能超过2GB");
        }

        String extension = getExtension(normalizedContentType);
        String fileName = UUID.randomUUID() + extension;
        Path directory = Paths.get(uploadPath).toAbsolutePath().normalize().resolve("content");
        Files.createDirectories(directory);

        Path target = directory.resolve(fileName).normalize();
        file.transferTo(target);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return Result.success(baseUrl + "/uploads/content/" + fileName);
    }

    private String getExtension(String contentType) {
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            case "video/mp4" -> ".mp4";
            default -> ".jpg";
        };
    }
}
