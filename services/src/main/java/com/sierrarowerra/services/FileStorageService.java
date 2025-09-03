package com.sierrarowerra.services;

public interface FileStorageService {
    String storeFile(byte[] content, String originalFilename, Long bikeId);

    void deleteFile(String fileName);
}
