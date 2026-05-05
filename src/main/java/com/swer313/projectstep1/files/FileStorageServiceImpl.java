package com.swer313.projectstep1.files;

import com.swer313.projectstep1.errors.BadRequestException;
import com.swer313.projectstep1.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public StoredFileResult storeHotelImage(MultipartFile file) {
        return storeImage(file);
    }

    @Override
    public StoredFileResult storeRoomTypeImage(MultipartFile file) {
        return storeImage(file);
    }

    @Override
    public StoredFileResult storeProfileImage(MultipartFile file) {
        return storeImage(file);
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("Invalid file path.");
        }

        try {
            Path uploadPath = getUploadPath();
            Path filePath = uploadPath.resolve(fileName).normalize();
            validateInsideUploadDir(uploadPath, filePath);

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not load file.", e);
        }
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String marker = "/uploads/";
            int index = fileUrl.indexOf(marker);
            if (index == -1) {
                return;
            }

            String fileName = fileUrl.substring(index + marker.length());
            Path uploadPath = getUploadPath();
            Path filePath = uploadPath.resolve(fileName).normalize();
            validateInsideUploadDir(uploadPath, filePath);

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file.", e);
        }
    }

    private StoredFileResult storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required.");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPG, PNG, and WEBP images are allowed.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size must not exceed 5 MB.");
        }

        try {
            Path uploadPath = getUploadPath();
            Files.createDirectories(uploadPath);

            String originalName = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
            );
            String extension = getExtension(originalName);
            String storedFileName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);

            Path filePath = uploadPath.resolve(storedFileName).normalize();
            validateInsideUploadDir(uploadPath, filePath);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = baseUrl + "/uploads/" + storedFileName;
            return new StoredFileResult(storedFileName, fileUrl);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file.", e);
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private void validateInsideUploadDir(Path uploadPath, Path filePath) {
        if (!filePath.startsWith(uploadPath)) {
            throw new BadRequestException("Invalid file path.");
        }
    }
}
