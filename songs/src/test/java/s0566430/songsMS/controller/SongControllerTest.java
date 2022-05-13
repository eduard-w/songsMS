package s0566430.songsMS.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import s0566430.songsMS.model.Song;
import s0566430.songsMS.repository.SongDao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SongControllerTest {

    private static final Song SONG_1 = new Song("Never Gonna Give You Up", "Rick Astley", "RCA", 1987);
    private static final Song SONG_2 = new Song("Never Gonna Give You Up Again", "Rick Astley", "RCA", 1989);
    //private static final User USER_1 = new User("user1", "12345", null, null);
    private static final String USER_ID = "user1";
    private static final String TOKEN = "abcde";

    private static final String FILE_PATH = "src/test/resources/payloads/songs/";
    private static String GET_SONG_JSON, GET_ALL_SONGS_JSON, GET_SONG_XML, GET_ALL_SONGS_XML, POST_SONG_JSON, POST_SONG_INVALID_JSON, UPDATE_SONG_INVALID_JSON;

    private MockMvc mvc;
    private SongDao dao;
    private AuthService helper;

    @BeforeEach
    void setUp() throws IOException {
        dao = Mockito.mock(SongDao.class);
        helper = Mockito.mock(AuthService.class);
        mvc = MockMvcBuilders.standaloneSetup(new SongController(dao, helper)).build();
        SONG_1.setId(1);
        SONG_2.setId(2);
        GET_SONG_JSON = Files.readString(Path.of(FILE_PATH + "getSongPayload.json"));
        POST_SONG_JSON = Files.readString(Path.of(FILE_PATH + "postSongPayload.json"));
        POST_SONG_INVALID_JSON = Files.readString(Path.of(FILE_PATH + "postSongPayloadInvalid.json"));
        GET_ALL_SONGS_JSON = Files.readString(Path.of(FILE_PATH + "getAllSongsPayload.json"));
        GET_SONG_XML = Files.readString(Path.of(FILE_PATH + "getSongPayload.xml"));
        GET_ALL_SONGS_XML = Files.readString(Path.of(FILE_PATH + "getAllSongsPayload.xml"));
        UPDATE_SONG_INVALID_JSON = Files.readString(Path.of(FILE_PATH + "updateSongPayloadInvalid.json"));
        //UserController.getTokenMap().put(TOKEN, USER_1);
        Mockito.when(helper.getUserIdForToken(anyString())).then(arg -> {
            if (arg.getArgument(0).equals(TOKEN)) return USER_ID; else return null;
        });
        Mockito.when(helper.doesTokenExist(any())).thenCallRealMethod();
        Mockito.when(helper.doesTokenMatchUserId(any(), any())).thenCallRealMethod();
    }

    private void setUpSong() {
        Mockito.when(dao.findSong(1)).thenReturn(SONG_1);
        Mockito.when(dao.findSong(5)).thenReturn(null);
    }

    private void setUpTwoSongs() {
        LinkedList<Song> songs = new LinkedList<>();
        songs.add(SONG_1);
        songs.add(SONG_2);
        Mockito.when(dao.findAllSongs()).thenReturn(songs);
    }

    // GET all songs

    @Test
    void getAllSongs_Json() throws Exception {
        setUpTwoSongs();

        mvc.perform(get("/").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(GET_ALL_SONGS_JSON));
    }

    @Test
    void getAllSongs_Xml() throws Exception {
        setUpTwoSongs();

        mvc.perform(get("/").accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(content().xml(GET_ALL_SONGS_XML));
    }

    @Test
    void getAllSongs_Empty() throws Exception {
        Mockito.when(dao.findAllSongs()).thenReturn(new LinkedList<>());

        mvc.perform(get("/").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllSongs_MediaTypeNotAcceptable() throws Exception {
        setUpTwoSongs();

        mvc.perform(get("/").accept(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void getAllSongs_MediaTypeMissing() throws Exception {
        setUpTwoSongs();

        mvc.perform(get("/").header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSongs_AuthTokenInvalid() throws Exception {
        setUpTwoSongs();

        mvc.perform(get("/").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN+'a'))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllSongs_AuthTokenMissing() throws Exception {
        setUpTwoSongs();

        mvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // GET one song

    @Test
    void getSong_Json() throws Exception {
        setUpSong();

        mvc.perform(get("/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(GET_SONG_JSON));
    }

    @Test
    void getSong_Xml() throws Exception {
        setUpSong();

        mvc.perform(get("/1").accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(content().xml(GET_SONG_XML));
    }

    @Test
    void getSong_IdInvalid() throws Exception {
        setUpSong();

        mvc.perform(get("/test").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSong_IdOutOfBounds() throws Exception {
        setUpSong();

        mvc.perform(get("/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSong_MediaTypeNotAcceptable() throws Exception {
        setUpSong();

        mvc.perform(get("/1").accept(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void getSong_MediaTypeMissing() throws Exception {
        setUpSong();

        mvc.perform(get("/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSong_AuthTokenInvalid() throws Exception {
        setUpSong();

        mvc.perform(get("/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN+'a'))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSong_AuthTokenMissing() throws Exception {
        setUpSong();

        mvc.perform(get("/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private AtomicReference<Song> createStub_SaveSong() {
        AtomicReference<Song> value = new AtomicReference<>();
        Mockito.when(dao.saveSong(any())).thenAnswer(invocation -> {
            value.set(invocation.getArgument(0));
            return 1;
        });
        return value;
    }

    // POST song

    @Test
    void postSong() throws Exception {
        AtomicReference<Song> song = createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Location", "/rest/songs/1"));

        assertEquals(0, song.get().getId()); // id is assigned by database
        assertEquals("Never Gonna Give You Up", song.get().getTitle());
        assertEquals("Rick Astley", song.get().getArtist());
        assertEquals("RCA", song.get().getLabel());
        assertEquals(1987, song.get().getReleased());
    }

    @Test
    void postSong_TitleOnly() throws Exception {
        AtomicReference<Song> song = createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{\"title\":\"Test Song\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Location", "/rest/songs/1"));

        assertEquals(0, song.get().getId());
        assertEquals("Test Song", song.get().getTitle());
        assertEquals(null, song.get().getArtist());
        assertEquals(null, song.get().getLabel());
        assertEquals(0, song.get().getReleased());
    }

    @Test
    void postSong_Two() throws Exception {
        createStub_SaveSong();
        // verify that request handler can run multiple times
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_JSON))
                .andExpect(status().isCreated());
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void postSong_JsonWithIdProperty() throws Exception {
        createStub_SaveSong();
        // payload in GET response contains field "id", so we're using it in our POST request
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(GET_SONG_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("song IDs are not to be manually assigned"))
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSong_MediaTypeMissing() throws Exception {
        createStub_SaveSong();

        mvc.perform(post("/").header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSong_MediaTypeUnsupported() throws Exception {
        createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSong_JsonSyntaxInvalid() throws Exception {
        createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_JSON.replaceFirst("(\")", "*")))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSong_JsonContentInvalid() throws Exception {
        createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(POST_SONG_INVALID_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSong_JsonEmpty() throws Exception {
        createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSong_AuthTokenInvalid() throws Exception {
        AtomicReference<Song> song = createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN+'a').content(POST_SONG_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postSong_AuthTokenMissing() throws Exception {
        AtomicReference<Song> song = createStub_SaveSong();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(POST_SONG_JSON))
                .andExpect(status().isUnauthorized());
    }

    // PUT song

    private AtomicReference<Song> createStub_UpdateSong() {
        AtomicReference<Song> value = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            value.set(invocation.getArgument(0));
            return null;
        }).when(dao).updateSong(any());
        return value;
    }

    @Test
    void updateSong() throws Exception {
        AtomicReference<Song> song = createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(GET_SONG_JSON))
                .andExpect(status().isNoContent());

        assertEquals("Never Gonna Give You Up", song.get().getTitle());
        assertEquals("Rick Astley", song.get().getArtist());
        assertEquals("RCA", song.get().getLabel());
        assertEquals(1987, song.get().getReleased());
    }

    @Test
    void updateSong_TitleOnly_IdMissing() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{\"title\":\"Test Song\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_TitleMissing() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{\"id\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_IdInconsistent() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/2").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(GET_SONG_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_IdInvalid() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/test").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{\"id\":\"test\",\"title\":\"Test Song\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_IdOutOfBounds() throws Exception {
        doThrow(new IndexOutOfBoundsException()).when(dao).updateSong(any());

        mvc.perform(put("/10").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{\"id\":10,\"title\":\"Test Song\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSong_MediaTypeMissing() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").header(HttpHeaders.AUTHORIZATION, TOKEN).content(GET_SONG_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void updateSong_MediaTypeUnsupported() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN).content(GET_SONG_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void updateSong_JsonSyntaxInvalid() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(GET_SONG_JSON.replaceFirst("(\")", "*")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_JsonContentInvalid() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content(UPDATE_SONG_INVALID_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_JsonEmpty() throws Exception {
        createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSong_AuthTokenInvalid() throws Exception {
        AtomicReference<Song> song = createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN+"a").content(GET_SONG_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSong_AuthTokenMissing() throws Exception {
        AtomicReference<Song> song = createStub_UpdateSong();

        mvc.perform(put("/1").contentType(MediaType.APPLICATION_JSON).content(GET_SONG_JSON))
                .andExpect(status().isUnauthorized());
    }

    // DELETE song

    @Test
    void deleteSong() throws Exception {
        mvc.perform(delete("/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isNoContent());
        Mockito.verify(dao).deleteSong(1);
    }

    @Test
    void deleteSong_IdOutOfBounds() throws Exception {
        doThrow(new IndexOutOfBoundsException()).when(dao).deleteSong(10);

        mvc.perform(delete("/10").header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isNotFound());
        Mockito.verify(dao).deleteSong(10);
    }

    @Test
    void deleteSong_IdInvalid() throws Exception {
        mvc.perform(delete("/test").header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(dao);
    }

    @Test
    void deleteSong_AuthTokenInvalid() throws Exception {
        mvc.perform(delete("/1").header(HttpHeaders.AUTHORIZATION, TOKEN+'a'))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteSong_AuthTokenMissing() throws Exception {
        mvc.perform(delete("/1"))
                .andExpect(status().isUnauthorized());
    }
}
