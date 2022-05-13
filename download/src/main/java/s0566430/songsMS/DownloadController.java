package s0566430.songsMS;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

@RestController
public class DownloadController {

    private StorageService storageService;
    private AuthService authService;

    public DownloadController(StorageService storageService, AuthService authService) {
        this.storageService = storageService;
        this.authService = authService;
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable int id, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) throws IOException {
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Resource file;
        try {
            file = storageService.load("song-"+id);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @PathVariable int id, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (!file.getContentType().equals("audio/mpeg"))
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        try {
            storageService.save(file, "song-"+id);
        }
        catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.created(URI.create("/download/"+id)).contentType(MediaType.TEXT_PLAIN).build();
    }
}
