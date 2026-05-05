package com.swer313.projectstep1.files;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFileResult storeHotelImage(MultipartFile file);
    StoredFileResult storeRoomTypeImage(MultipartFile file);
    StoredFileResult storeProfileImage(MultipartFile file);
    Resource loadFileAsResource(String fileName);
    void deleteFileByUrl(String fileUrl);
}
