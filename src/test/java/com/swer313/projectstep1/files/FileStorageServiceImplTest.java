package com.swer313.projectstep1.files;

import com.swer313.projectstep1.errors.BadRequestException;
import com.swer313.projectstep1.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl createWithDir(Path dir) throws Exception {
        FileStorageServiceImpl svc = new FileStorageServiceImpl();
        Field f1 = FileStorageServiceImpl.class.getDeclaredField("uploadDir");
        f1.setAccessible(true);
        f1.set(svc, dir.toString());

        Field f2 = FileStorageServiceImpl.class.getDeclaredField("baseUrl");
        f2.setAccessible(true);
        f2.set(svc, "http://localhost:8080");

        return svc;
    }

    @Test
    void storeHotelImage_allowedType_succeeds_and_deleteRemovesFile() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);

        MockMultipartFile file = new MockMultipartFile(
                "file", "pic.png", "image/png", "data".getBytes()
        );

        StoredFileResult res = svc.storeHotelImage(file);
        assertThat(res).isNotNull();
        assertThat(res.getFileName()).isNotBlank();
        assertThat(res.getFileUrl()).contains("http://localhost:8080/uploads/");

        // file on disk
        Path stored = tempDir.resolve(res.getFileName());
        assertThat(Files.exists(stored)).isTrue();

        // delete
        svc.deleteFileByUrl(res.getFileUrl());
        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void storeHotelImage_invalidType_throwsBadRequest() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "hi".getBytes()
        );

        assertThatThrownBy(() -> svc.storeHotelImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only JPG, PNG, and WEBP images are allowed");
    }

    @Test
    void storeHotelImage_tooLarge_throwsBadRequest() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);
        byte[] content = new byte[(5 * 1024 * 1024) + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", content
        );

        assertThatThrownBy(() -> svc.storeHotelImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("File size must not exceed 5 MB.");
    }

    @Test
    void storeProfileImage_usesSharedImageStorage() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.webp", "image/webp", "profile".getBytes()
        );

        StoredFileResult res = svc.storeProfileImage(file);

        assertThat(res.getFileName()).endsWith(".webp");
        assertThat(Files.exists(tempDir.resolve(res.getFileName()))).isTrue();
    }

    @Test
    void loadFileAsResource_existingFile_returnsResource() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);
        Path stored = tempDir.resolve("safe.png");
        Files.writeString(stored, "data");

        Resource resource = svc.loadFileAsResource("safe.png");

        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void loadFileAsResource_missingFile_throwsResourceNotFound() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);

        assertThatThrownBy(() -> svc.loadFileAsResource("missing.png"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("File not found: missing.png");
    }

    @Test
    void loadFileAsResource_pathTraversal_throwsBadRequest() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);

        assertThatThrownBy(() -> svc.loadFileAsResource("../outside.png"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid file path.");
    }

    @Test
    void deleteFileByUrl_pathTraversal_throwsBadRequest() throws Exception {
        FileStorageServiceImpl svc = createWithDir(tempDir);

        assertThatThrownBy(() -> svc.deleteFileByUrl("http://localhost:8080/uploads/../outside.png"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid file path.");
    }
}

