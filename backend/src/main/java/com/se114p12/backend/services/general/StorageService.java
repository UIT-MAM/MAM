package com.se114p12.backend.services.general;

import com.se114p12.backend.exceptions.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

  private final Path rootLocation;

  public StorageService(@Value("${storage.root.location}") String uploadPath) {
    this.rootLocation = Paths.get(uploadPath);
    init();
  }

  public void init() {
    try {
      if (!Files.exists(rootLocation)) {
        Files.createDirectories(rootLocation);
      }
    } catch (IOException e) {
      throw new StorageException("Could not initialize storage", e);
    }
  }

  /**
   * @return file URI from root path
   */
  public String store(MultipartFile file, String folder) {
    if (file.isEmpty()) throw new StorageException("Failed to store empty file");
    if (!Files.exists(rootLocation.resolve(folder))) {
      try {
        Files.createDirectories(rootLocation.resolve(folder));
      } catch (IOException e) {
        throw new StorageException("Could not create directory", e);
      }
    }
    String filename = System.currentTimeMillis() + "-" + file.getOriginalFilename();
    Path fileUri = rootLocation.resolve(folder).resolve(filename);
    try {
      Files.copy(file.getInputStream(), fileUri, StandardCopyOption.REPLACE_EXISTING);
      return rootLocation.relativize(fileUri).toString();
    } catch (IOException e) {
      throw new StorageException("Failed to store file", e);
    }
  }

  // Stream must be closed by the caller
  public String storeByInputStream(InputStream stream, String folder) {
    if (!Files.exists(rootLocation.resolve(folder))) {
      try {
        Files.createDirectories(rootLocation.resolve(folder));
      } catch (IOException e) {
        throw new StorageException("Could not create directory", e);
      }
    }
    String filename = UUID.randomUUID().toString();
    Path fileUri = rootLocation.resolve(folder).resolve(filename);
    try {
      Files.copy(stream, fileUri, StandardCopyOption.REPLACE_EXISTING);
      return rootLocation.relativize(fileUri).toString();
    } catch (IOException e) {
      throw new StorageException("Failed to store file", e);
    }
  }

  public Path loadPath(String filename, String folder) {
    if (!Files.exists(rootLocation.resolve(folder).resolve(filename)))
      throw new StorageException("File not found");
    return rootLocation.resolve(folder).resolve(filename);
  }

  public Resource loadAsResource(String filename, String folder) {
    try {
      Path file = loadPath(filename, folder);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new StorageException("Could not read file");
      }
    } catch (IOException e) {
      throw new StorageException("Could not read file", e);
    }
  }

  public Resource loadAsResource(String url) {
    try {
      Path file = rootLocation.resolve(url);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new StorageException("Could not read file");
      }
    } catch (IOException e) {
      throw new StorageException("Could not read file", e);
    }
  }

  public void delete(String uri) {
    try {
      Files.deleteIfExists(rootLocation.resolve(uri));
    } catch (IOException e) {
      throw new StorageException("Failed to delete file", e);
    }
  }
}
