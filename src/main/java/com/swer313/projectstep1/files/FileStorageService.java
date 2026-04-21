package com.swer313.projectstep1.files;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFileResult storeHotelImage(MultipartFile file);
    void deleteFileByUrl(String fileUrl);
    StoredFileResult storeRoomTypeImage(MultipartFile file);
}