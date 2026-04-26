package com.melikyan.academy.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.melikyan.academy.exception.BadRequestException;

import java.util.UUID;
import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class CertificateStorageService {
    private static final Path ROOT = Path.of("uploads", "certificates");

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private boolean hasPdfSignature(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(5);

            if (header.length < 5) {
                return false;
            }

            return header[0] == '%'
                    && header[1] == 'P'
                    && header[2] == 'D'
                    && header[3] == 'F'
                    && header[4] == '-';
        } catch (IOException exception) {
            throw new BadRequestException("Failed to read certificate PDF");
        }
    }

    public String saveCertificatePdf(MultipartFile file, UUID certificateId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Certificate PDF file is required");
        }

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);

        if (!extension.equalsIgnoreCase(".pdf")) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        if (!hasPdfSignature(file)) {
            throw new BadRequestException("Only valid PDF files are allowed");
        }

        try {
            Files.createDirectories(ROOT);

            String fileName = certificateId + "_" + UUID.randomUUID() + ".pdf";
            Path target = ROOT.resolve(fileName);

            System.out.println("Saving certificate PDF to: " + target.toAbsolutePath());

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/certificates/" + fileName;
        } catch (IOException exception) {
            throw new BadRequestException("Failed to save certificate PDF");
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
            throw new BadRequestException("Failed to delete certificate PDF");
        }
    }
}