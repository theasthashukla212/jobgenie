package com.jobgenie.jobgenie_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.jobgenie.jobgenie_backend.exception.ResourceNotFoundException;

@Service
public class ResumeFileStorageService {
    private final Path root;

    public ResumeFileStorageService(@Value("${app.resume.storage-dir:./uploads/resumes}") String storageDirectory) {
        this.root = Paths.get(storageDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize resume storage", exception);
        }
    }

    public String store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Resume file is required");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("Resume file must be at most 5 MB");
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equalsIgnoreCase("application/pdf")
                || contentType.equalsIgnoreCase("application/msword")
                || contentType.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equalsIgnoreCase("text/plain"))) {
            throw new IllegalArgumentException("Resume file must be PDF, DOC, DOCX, or plain text");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension == null ? "" : "." + extension.toLowerCase());
        Path userDirectory = root.resolve(String.valueOf(userId)).normalize();
        try {
            Files.createDirectories(userDirectory);
            Path destination = userDirectory.resolve(filename).normalize();
            if (!destination.startsWith(userDirectory)) throw new IllegalArgumentException("Invalid resume filename");
            Files.copy(file.getInputStream(), destination);
            return userId + "/" + filename;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store resume file", exception);
        }
    }

    public Resource load(String storedPath) {
        try {
            Path file = root.resolve(storedPath).normalize();
            if (!file.startsWith(root) || !Files.exists(file)) throw new ResourceNotFoundException("Resume file not found");
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new ResourceNotFoundException("Resume file not found");
            return resource;
        } catch (IOException exception) {
            throw new ResourceNotFoundException("Resume file not found");
        }
    }
}
