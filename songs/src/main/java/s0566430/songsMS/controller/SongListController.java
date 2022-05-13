package s0566430.songsMS.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s0566430.songsMS.model.SongList;
import s0566430.songsMS.model.SongListsXmlRoot;
import s0566430.songsMS.repository.SongDao;
import s0566430.songsMS.repository.SongListDao;

import javax.persistence.PersistenceException;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static s0566430.songsMS.controller.AuthService.convertSongListToXml;
import static s0566430.songsMS.controller.AuthService.getStackTrace;

@RestController
@RequestMapping(value = "/playlists")
public class SongListController {

    private SongListDao songListDao;
    private SongDao songDao;
    private AuthService authService;
    private ObjectMapper mapper = new ObjectMapper();

    public SongListController(SongListDao songListDao, SongDao songDao, AuthService authService) {
        this.songListDao = songListDao;
        this.songDao = songDao;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<String> getAllListsByUserId(
            @RequestHeader(HttpHeaders.ACCEPT) String accept,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @RequestParam String userId
    ) throws IOException, JAXBException {
        if (!authService.doesTokenExist(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!authService.doesUserIdExist(userId)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // TODO: add error handling

        List<SongList> songLists;
        if (authService.doesTokenMatchUserId(auth, userId)) {
            songLists = songListDao.findAllListsByUserId(userId);
        } else {
            songLists = songListDao.findAllPublicListsByUserId(userId);
        }

        switch (accept) {
            case MediaType.APPLICATION_JSON_VALUE:
                return new ResponseEntity<>(mapper.writeValueAsString(songLists), HttpStatus.OK);
            case MediaType.APPLICATION_XML_VALUE:
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(convertSongListToXml(new SongListsXmlRoot(songLists)));
            default:
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> getListById(
            @RequestHeader(HttpHeaders.ACCEPT) String accept,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
            @PathVariable Integer id
    ) throws IOException, JAXBException {
        if (!authService.doesTokenExist(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        SongList songList = songListDao.findListById(id);
        if (songList == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (!authService.doesTokenMatchUserId(auth, songList.getOwnerId()) && songList.getIsPrivate()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        switch (accept) {
            case MediaType.APPLICATION_JSON_VALUE:
                return new ResponseEntity<>(mapper.writeValueAsString(songList), HttpStatus.OK);
            case MediaType.APPLICATION_XML_VALUE:
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(convertSongListToXml(new SongListsXmlRoot(songList)));
            default:
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postList(@RequestBody String listJson, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) throws IOException {
        if (!authService.doesTokenExist(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        int listId = -1;
        try {
            if (listJson.toLowerCase().contains("\"listId\":"))
                return ResponseEntity.badRequest().body("list IDs are not to be manually assigned");
            SongList songList = mapper.readValue(listJson, SongList.class);
            if (songList.getListName() == null)
                throw new IllegalArgumentException("property 'name' must be provided");
            if (!doAllSongsExist(songList))
                return ResponseEntity.badRequest().body("invalid Song information, please match with database entries");
            songList.setOwnerId(authService.getUserIdForToken(auth));
            listId = songListDao.saveList(songList);
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(getStackTrace(e));
        }
        return ResponseEntity.created(URI.create("/rest/playlists/" + listId)).contentType(MediaType.TEXT_PLAIN).build();
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{id}")
    public ResponseEntity<String> updateList(@RequestBody String listJson, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable int id) throws IOException {
        if (!authService.doesTokenExist(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            if (id == 0)
                throw new IllegalArgumentException("path variable 'listId' must be provided and can not be 0");

            SongList songListNew = mapper.readValue(listJson, SongList.class);
            if (!doAllSongsExist(songListNew))
                return ResponseEntity.badRequest().body("invalid SongList information, please match with database entries");

            SongList songListOriginal = songListDao.findListById(id);
            if (songListOriginal == null)
                return ResponseEntity.notFound().build();
            if (!authService.doesTokenMatchUserId(auth, songListOriginal.getOwnerId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            // only update contents of json, keep other properties the same
            songListNew.setListId(id);
            songListNew.setOwnerId(songListOriginal.getOwnerId());
            if (!listJson.contains("\"isPrivate\":"))
                songListNew.setIsPrivate(songListOriginal.getIsPrivate());
            if (songListNew.getListName() == null)
                songListNew.setListName(songListOriginal.getListName());
            if (songListNew.getSongs() == null)
                songListNew.setSongs(songListOriginal.getSongs());
            songListDao.updateList(songListNew);
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(getStackTrace(e));
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private boolean doAllSongsExist(SongList songList) {
        return songDao.findAllSongs().containsAll(songList.getSongs());
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteListById(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth, @PathVariable Integer id) throws IOException {
        if (!authService.doesTokenExist(auth)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        SongList songList = songListDao.findListById(id);
        if (songList == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (!authService.doesTokenMatchUserId(auth, songList.getOwnerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            songListDao.deleteList(id);
        } catch (IndexOutOfBoundsException e) {
            return ResponseEntity.notFound().build();
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        }
        return ResponseEntity.noContent().build();
    }
}
