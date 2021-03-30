package com.viettel.vtag.service.impl;

import com.viettel.vtag.service.interfaces.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path location;

    public StorageServiceImpl(@Value("${vtag.storage.location}") String location) {
        this.location = Paths.get(location);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(location);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(String folder, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            var destinationFile = location.resolve(Paths.get(folder, file.getOriginalFilename()))
                .normalize()
                .toAbsolutePath();
            if (!destinationFile.getParent().equals(location.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return "";
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.location, 1)
                .filter(path -> !path.equals(this.location))
                .map(this.location::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return location.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) throws FileNotFoundException {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not read file '" + filename + "': " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(location.toFile());
    }
}
