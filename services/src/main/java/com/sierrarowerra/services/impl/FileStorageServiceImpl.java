package com.sierrarowerra.services.impl;

import com.sierrarowerra.services.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(byte[] content, String originalFilename, Long bikeId) {
        Path bikeDirectory = this.fileStorageLocation.resolve(String.valueOf(bikeId));
        try {
            Files.createDirectories(bikeDirectory);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for the bike.", ex);
        }

        String originalFileName = StringUtils.cleanPath(originalFilename);
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            // ignore
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if(uniqueFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + uniqueFileName);
            }

            Path targetLocation = bikeDirectory.resolve(uniqueFileName);
            Files.copy(new ByteArrayInputStream(content), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return String.valueOf(bikeId) + "/" + uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + uniqueFileName + ". Please try again!", ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);

            Path parentDir = filePath.getParent();
            if (parentDir != null && Files.isDirectory(parentDir)) {
                try (Stream<Path> entries = Files.list(parentDir)) {
                    if (entries.findFirst().isEmpty()) {
                        Files.deleteIfExists(parentDir);
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileName, ex);
        }
    }
}
