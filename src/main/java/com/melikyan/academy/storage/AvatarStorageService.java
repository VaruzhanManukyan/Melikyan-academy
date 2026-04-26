package com.melikyan.academy.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.melikyan.academy.exception.BadRequestException;

import java.util.UUID;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

@Service
public class AvatarStorageService {
    private static final Path ROOT = Path.of("uploads", "avatars");

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    public String saveAvatar(MultipartFile file, UUID userId) {
        try {
            Files.createDirectories(ROOT);

            String originalName = file.getOriginalFilename();
            String extension = getExtension(originalName);
            String fileName = userId + "_" + UUID.randomUUID() + extension;

            Path target = ROOT.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + fileName;
        } catch (IOException exception) {
            throw new BadRequestException("Failed to save avatar");
        }
    }

    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        try {
            String relative = path.replaceFirst("^/uploads/", "");
            Path filePath = Path.of("uploads").resolve(relative);
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new BadRequestException("Failed to delete avatar");
        }
    }
}
