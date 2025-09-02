package com.sierrarowerra.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, Long bikeId);

    void deleteFile(String fileName);
}
