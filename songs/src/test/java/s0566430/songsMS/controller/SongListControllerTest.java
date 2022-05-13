package s0566430.songsMS.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import s0566430.songsMS.model.Song;
import s0566430.songsMS.model.SongList;
import s0566430.songsMS.repository.SongDao;
import s0566430.songsMS.repository.SongListDao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SongListControllerTest {

    private static final String TOKEN_1 = "abcde";
    private static final String TOKEN_2 = "fghij";
    private static final String TOKEN_3 = "klmno";
    private static final String USER_ID_1 = "user1";
    private static final String USER_ID_2 = "user2";
    private static final String USER_ID_3 = "user3";

    private static final SongList SONG_LIST_PRIVATE_1 = new SongList(USER_ID_1, "list1", true);
    private static final SongList SONG_LIST_PRIVATE_2 = new SongList(USER_ID_1, "list2", true);
    private static final SongList SONG_LIST_PRIVATE_3 = new SongList(USER_ID_2, "list3", true);
    private static final SongList SONG_LIST_PUBLIC = new SongList(USER_ID_2, "list4", false);

    private static final Song SONG_1 = new Song("We Built This City", "Starship", "Grunt/RCA", 1985);
    private static final Song SONG_2 = new Song("Sussudio", "Phil Collins", "Virgin", 1985);
    private static final Song SONG_3 = new Song("Sus Amogus", "Phil Collinsus", "Virgin", 1984);
    private static final Song SONG_4 = new Song("Sussudio 2", "Phil Collins", "Chad", 1986);

    private static final String FILE_PATH = "src/test/resources/payloads/songLists/";
    private static String GET_SONGLIST_JSON, GET_ALL_SONGLISTS_JSON, GET_SONGLIST_XML, GET_ALL_SONGLISTS_XML, POST_SONGLIST_JSON, POST_SONGLIST_JSON_EMPTY, POST_SONGLIST_JSON_INVALID, UPDATE_SONGLIST_JSON, UPDATE_SONGLIST_JSON_INVALID_CONTENT, UPDATE_SONGLIST_JSON_INVALID;

    private MockMvc mvc;
    private SongListDao songListDao;
    private SongDao songDao;
    private AuthService helper;

    @BeforeEach
    void setUp() throws IOException {
        songListDao = Mockito.mock(SongListDao.class);
        songDao = Mockito.mock(SongDao.class);
        helper = Mockito.mock(AuthService.class);
        mvc = MockMvcBuilders.standaloneSetup(new SongListController(songListDao, songDao, helper)).build();

        GET_SONGLIST_JSON = Files.readString(Path.of(FILE_PATH + "getSongList.json"));
        GET_ALL_SONGLISTS_JSON = Files.readString(Path.of(FILE_PATH + "getAllSongLists.json"));
        GET_ALL_SONGLISTS_XML = Files.readString(Path.of(FILE_PATH + "getAllSongLists.xml"));
        GET_SONGLIST_XML = Files.readString(Path.of(FILE_PATH + "getSongList.xml"));
        POST_SONGLIST_JSON = Files.readString(Path.of(FILE_PATH + "postSongList.json"));
        POST_SONGLIST_JSON_EMPTY = Files.readString(Path.of(FILE_PATH + "postSongListEmpty.json"));
        POST_SONGLIST_JSON_INVALID = Files.readString(Path.of(FILE_PATH + "postSongListInvalid.json"));
        UPDATE_SONGLIST_JSON = Files.readString(Path.of(FILE_PATH + "updateSongList.json"));
        UPDATE_SONGLIST_JSON_INVALID_CONTENT = Files.readString(Path.of(FILE_PATH + "updateSongListInvalidSong.json"));
        UPDATE_SONGLIST_JSON_INVALID = Files.readString(Path.of(FILE_PATH + "updateSongListInvalid.json"));

        Mockito.when(helper.getUserIdForToken(anyString())).then(arg -> {
            switch ((String) arg.getArgument(0)) {
                case TOKEN_1: return USER_ID_1;
                case TOKEN_2: return USER_ID_2;
                case TOKEN_3: return USER_ID_3;
                default: return null;
            }
        });
        Mockito.when(helper.doesTokenExist(any())).thenCallRealMethod();
        Mockito.when(helper.doesTokenMatchUserId(any(), any())).thenCallRealMethod();
        Mockito.when(helper.doesUserIdExist(anyString())).then(arg -> {
            switch ((String) arg.getArgument(0)) {
                case USER_ID_1:
                case USER_ID_2:
                case USER_ID_3:
                    return true;
                default:
                    return false;
            }
        });
        SONG_1.setId(5);
        SONG_2.setId(4);
        SONG_4.setId(3);
        Mockito.when(songDao.findAllSongs()).thenReturn(Arrays.asList(SONG_1, SONG_2, SONG_3, SONG_4));
    }

    void setUpSongLists() {
        SONG_LIST_PRIVATE_1.setSongs(Arrays.asList(SONG_1, SONG_2));
        SONG_LIST_PRIVATE_1.setListId(1);
        SONG_LIST_PRIVATE_2.setSongs(Arrays.asList(SONG_2));
        SONG_LIST_PRIVATE_2.setListId(2);

        Mockito.when(songListDao.findAllListsByUserId("user1"))
                .thenReturn(Arrays.asList(new SongList[]{SONG_LIST_PRIVATE_1, SONG_LIST_PRIVATE_2}));
        Mockito.when(songListDao.findAllListsByUserId("user2"))
                .thenReturn(Arrays.asList(new SongList[]{SONG_LIST_PUBLIC}));

        Mockito.when(songListDao.findListById(1)).thenReturn(SONG_LIST_PRIVATE_1);
        Mockito.when(songListDao.findListById(2)).thenReturn(SONG_LIST_PRIVATE_2);
        Mockito.when(songListDao.findListById(3)).thenReturn(SONG_LIST_PUBLIC);
    }

    // GET all songlists by user

    @Test
    void getAllSongListsByUser_Json() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(GET_ALL_SONGLISTS_JSON, true));

        Mockito.verify(songListDao).findAllListsByUserId("user1");
    }

    @Test
    void getAllSongListsByUser_Xml() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user1").accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andExpect(content().xml(GET_ALL_SONGLISTS_XML));
    }

    @Test
    void getAllSongListsByUser_Empty() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_3))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    void getAllSongListsByUser_ForeignOwner() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));

        Mockito.verify(songListDao).findAllPublicListsByUserId("user2");
    }

    @Test
    void getAllSongListsByUser_UserNotFound() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user4").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSongListsByUser_MediaTypeNotAcceptable() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user1").accept(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void getAllSongListsByUser_MediaTypeMissing() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user1").header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllSongListsByUser_AuthTokenInvalid() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1+'a'))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllSongListsByUser_AuthTokenMissing() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists?userId=user1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // GET songlist by id

    @Test
    void getSongList_Json() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(GET_SONGLIST_JSON, true));
    }

    @Test
    void getSongList_Xml() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").accept(MediaType.APPLICATION_XML).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML))
                .andDo((value) -> {
                    System.out.println(value.getResponse().getContentAsString());
                    System.out.println(GET_SONGLIST_XML);
                })
                .andExpect(content().xml(GET_SONGLIST_XML));
    }

    @Test
    void getSongList_ForeignOwner() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_2))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSongList_IdInvalid() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/test").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSongList_IdOutOfBounds() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/10").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSongList_MediaTypeNotAcceptable() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").accept(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void getSongList_MediaTypeMissing() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSongList_AuthTokenInvalid() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1+'a'))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSongList_AuthTokenMissing() throws Exception {
        setUpSongLists();

        mvc.perform(get("/playlists/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // POST songlist

    private AtomicReference<SongList> createStub_SaveSongList() {
        AtomicReference<SongList> value = new AtomicReference<>();
        Mockito.when(songListDao.saveList(any())).thenAnswer(invocation -> {
            value.set(invocation.getArgument(0));
            return 1;
        });
        return value;
    }

    @Test
    void postSongList() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(POST_SONGLIST_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Location", "/rest/playlists/1"));

        assertEquals(0, songList.get().getListId()); // id is assigned by database
        assertEquals("list3", songList.get().getListName());
        assertEquals(true, songList.get().getIsPrivate());
        assertEquals(USER_ID_1, songList.get().getOwnerId());
        assertEquals(SONG_1, songList.get().getSongs().get(0));
        assertEquals(SONG_2, songList.get().getSongs().get(1));
    }

    @Test
    void postSongList_Empty() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(POST_SONGLIST_JSON_EMPTY))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Location", "/rest/playlists/1"));

        assertEquals(0, songList.get().getListId()); // id is assigned by database
        assertEquals("list1", songList.get().getListName());
        assertEquals(true, songList.get().getIsPrivate());
        assertEquals(USER_ID_1, songList.get().getOwnerId());
        assertTrue(songList.get().getSongs().isEmpty());
    }

    @Test
    void postSongList_SongsInvalid() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(POST_SONGLIST_JSON_INVALID))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSongList_JsonEmpty() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSongList_MediaTypeMissing() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(POST_SONGLIST_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSongList_MediaTypeUnsupported() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").contentType(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(POST_SONGLIST_JSON))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSongList_JsonSyntaxInvalid() throws Exception {
        AtomicReference<SongList> songList = createStub_SaveSongList();
        setUpSongLists();

        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(POST_SONGLIST_JSON.replaceFirst("(\")", "*")))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void postSongList_AuthTokenInvalid() throws Exception {
        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1+'a').content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postSongList_AuthTokenMissing() throws Exception {
        mvc.perform(post("/playlists").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // PUT songlist

    private AtomicReference<SongList> createStub_UpdateSongList() {
        AtomicReference<SongList> value = new AtomicReference<>();
        Mockito.when(songListDao.updateList(any())).then(invocation -> {
            value.set(invocation.getArgument(0));
            return null;
        });
        return value;
    }

    @Test
    void updateSongList() throws Exception {
        setUpSongLists();
        AtomicReference<SongList> songList = createStub_UpdateSongList();

        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isNoContent());

        assertEquals("user1", songList.get().getOwnerId());
        assertEquals("favs1", songList.get().getListName());
        assertEquals(1, songList.get().getListId());
        assertEquals(true, songList.get().getIsPrivate());
        assertEquals(SONG_1, songList.get().getSongs().get(0));
        assertEquals(SONG_4, songList.get().getSongs().get(1));
    }

    @Test
    void updateSongList_IdMissing() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void updateSongList_TitleMissing() throws Exception {
        setUpSongLists();
        AtomicReference<SongList> songList = createStub_UpdateSongList();

        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON.replace("\"listName\": \"favs1\",","")))
                .andExpect(status().isNoContent());

        assertEquals("user1", songList.get().getOwnerId());
        assertEquals("list1", songList.get().getListName());
        assertEquals(1, songList.get().getListId());
        assertEquals(true, songList.get().getIsPrivate());
        assertEquals(SONG_1, songList.get().getSongs().get(0));
        assertEquals(SONG_4, songList.get().getSongs().get(1));
    }

    @Test
    void updateSongList_IdInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/test").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSongList_IdOutOfBounds() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/10").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSongList_MediaTypeMissing() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void updateSongList_MediaTypeUnsupported() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void updateSongList_SongInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON_INVALID_CONTENT))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSongList_JsonSyntaxInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON.replaceFirst("(\")", "*")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSongList_JsonContentInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content(UPDATE_SONGLIST_JSON_INVALID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSongList_JsonEmpty() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1).content("{}"))
                .andExpect(status().isNoContent()); // perform no updates on songlist
    }

    @Test
    void updateSongList_AuthTokenInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1+"a").content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void updateSongList_AuthTokenMissing() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateSongList_AuthTokenFromOtherUser() throws Exception {
        setUpSongLists();
        mvc.perform(put("/playlists/1").contentType(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_2).content(UPDATE_SONGLIST_JSON))
                .andExpect(status().isForbidden());
    }

    // DELETE songlist

    @Test
    void deleteSongList() throws Exception {
        setUpSongLists();
        mvc.perform(delete("/playlists/1").header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isNoContent());
        Mockito.verify(songListDao).deleteList(1);
    }

    @Test
    void deleteSongList_IdOutOfBounds() throws Exception {
        setUpSongLists();
        mvc.perform(delete("/playlists/10").header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSongList_IdInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(delete("/playlists/test").header(HttpHeaders.AUTHORIZATION, TOKEN_1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSongList_ForeignOwner() throws Exception {
        setUpSongLists();
        mvc.perform(delete("/playlists/1").header(HttpHeaders.AUTHORIZATION, TOKEN_2))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSongList_AuthTokenInvalid() throws Exception {
        setUpSongLists();
        mvc.perform(delete("/playlists/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, TOKEN_1+'a'))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteSongList_AuthTokenMissing() throws Exception {
        setUpSongLists();
        mvc.perform(delete("/playlists/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
