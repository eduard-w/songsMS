package s0566430.songsMS.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s0566430.songsMS.model.Song;
import s0566430.songsMS.model.SongsXmlRoot;
import s0566430.songsMS.repository.SongDao;

import javax.persistence.PersistenceException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;

import static s0566430.songsMS.controller.AuthService.convertSongToXml;
import static s0566430.songsMS.controller.AuthService.getStackTrace;

@RestController
@RequestMapping
public class SongController {

    private SongDao dao;
    private ObjectMapper mapper = new ObjectMapper();
    private AuthService authService;

    public SongController(SongDao dao, AuthService authService) {
        this.dao = dao;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<String> getAllSongs(@RequestHeader(HttpHeaders.ACCEPT) String accept, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) throws IOException, JAXBException {
        // checks for auth token in usercontroller hashmap
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        switch (accept) {
            case MediaType.APPLICATION_JSON_VALUE:
                return new ResponseEntity<>(mapper.writeValueAsString(dao.findAllSongs()), HttpStatus.OK);
            case MediaType.APPLICATION_XML_VALUE:
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(convertSongToXml(new SongsXmlRoot(dao.findAllSongs())));
            default:
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> getSong(@RequestHeader(HttpHeaders.ACCEPT) String accept, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Integer id) throws IOException, JAXBException {
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Song song = dao.findSong(id);
        if (song == null) return ResponseEntity.notFound().build();

        switch (accept) {
            case MediaType.APPLICATION_JSON_VALUE:
                return new ResponseEntity<>(mapper.writeValueAsString(song), HttpStatus.OK);
            case MediaType.APPLICATION_XML_VALUE:
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(convertSongToXml(new SongsXmlRoot(song)));
            default:
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postSong(@RequestBody String songJson, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) throws IOException {
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        int songId = -1;
        try {
            if (songJson.toLowerCase().contains("\"id\":"))
                return ResponseEntity.badRequest().body("song IDs are not to be manually assigned");
            Song song = mapper.readValue(songJson, Song.class);
            if (song.getTitle() == null)
                throw new IllegalArgumentException("property 'title' must be provided");
            songId = dao.saveSong(song);
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(getStackTrace(e));
        }
        return ResponseEntity.created(URI.create("/rest/songs/" + songId)).contentType(MediaType.TEXT_PLAIN).build();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{id}")
    public ResponseEntity<String> updateSong(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Integer id, @RequestBody String songJson) throws IOException {
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Song song = mapper.readValue(songJson, Song.class);
            if (song.getTitle() == null)
                throw new IllegalArgumentException("property 'title' must be provided");
            if (song.getId() == 0)
                throw new IllegalArgumentException("property 'id' must be provided and can not be 0");
            else if (song.getId() != id)
                throw new IllegalArgumentException("property 'id' and path variable must be identical");
            dao.updateSong(song);
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(getStackTrace(e));
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteSong(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Integer id) throws IOException {
        if (!authService.doesTokenExist(auth))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            dao.deleteSong(id);
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.notFound().build();
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        }
        return ResponseEntity.noContent().build();
    }


}
