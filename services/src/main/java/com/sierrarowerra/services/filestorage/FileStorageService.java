package com.sierrarowerra.services.filestorage;

public interface FileStorageService {
    String storeFile(byte[] content, String originalFilename, Long bikeId);

    void deleteFile(String fileName);
}
