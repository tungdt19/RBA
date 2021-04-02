package com.viettel.vtag.service.impl;

import com.viettel.vtag.service.interfaces.StorageService;
import lombok.Data;
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

@Data
@Service
public class StorageServiceImpl implements StorageService {

    @Value("${vtag.storage.location}")
    private String location;

    private Path storage;

    @PostConstruct
    public void init() {
        try {
            this.storage = Paths.get(location);
            Files.createDirectories(storage);
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
            var destinationFile = storage.resolve(Paths.get(folder, file.getOriginalFilename()))
                .normalize()
                .toAbsolutePath();
            if (!destinationFile.getParent().equals(storage.toAbsolutePath())) {
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
            return Files.walk(storage, 1).filter(path -> !path.equals(storage)).map(storage::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return storage.resolve(filename);
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
        FileSystemUtils.deleteRecursively(storage.toFile());
    }
}
