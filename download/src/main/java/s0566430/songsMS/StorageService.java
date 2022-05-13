package s0566430.songsMS;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {

    private final Path location;

    public StorageService(@Value("${storage.location}") String path) {
        this.location = Paths.get(path);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(location);
    }

    public String save(MultipartFile file, String filename) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("empty file: " + filename);
        }
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, location.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        }
        return filename;
    }

    public Resource load(String filename) throws IOException {
        Path file = location.resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists())
            throw new FileNotFoundException("unknown file: "+filename);
        return resource;
    }
}