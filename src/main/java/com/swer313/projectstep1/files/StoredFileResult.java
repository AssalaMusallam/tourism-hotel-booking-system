package com.swer313.projectstep1.files;

public class StoredFileResult {

    private final String fileName;
    private final String fileUrl;

    public StoredFileResult(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}