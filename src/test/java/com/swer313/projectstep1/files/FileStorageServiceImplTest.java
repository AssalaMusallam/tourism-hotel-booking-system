package com.swer313.projectstep1.files;

import com.swer313.projectstep1.errors.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
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
}

