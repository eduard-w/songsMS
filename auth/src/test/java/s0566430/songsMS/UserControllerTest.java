package s0566430.songsMS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import s0566430.songsMS.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {
    private MockMvc mvc;
    private UserDao dao;
    private UserController ucSpy;

    private User testUser;

    private static final String LOGIN_JSON = "{\"userId\":\"mmuster\",\"password\":\"pass1234\"}";
    private static final String WRONG_PASSWORD_JSON = "{\"userId\":\"mmuster\",\"password\":\"geheim\"}";
    private static final String WRONG_USERID_JSON = "{\"userId\":\"falsch\",\"password\":\"pass1234\"}";
    private static final String FULL_USER_JSON = "{\"userId\":\"mmuster\",\"password\":\"pass1234\",\"firstName\":\"Maxime\",\"lastName\":\"Muster\"}";
    private static final String RESPONSE_TOKEN = "abcdefghijklmo";

    @BeforeEach
    void setUp() {
        dao = Mockito.mock(UserDao.class);
        UserController uc = new UserController(dao);
        ucSpy = Mockito.spy(uc);
        mvc = MockMvcBuilders.standaloneSetup(ucSpy).build();
        testUser = new User("mmuster","pass1234","Maxime","Muster");
    }

    @Test
    void login_successful() throws Exception {
        Mockito.when(dao.findUser("mmuster")).thenReturn(testUser);
        Mockito.doReturn(RESPONSE_TOKEN).when(ucSpy).generateResponseToken();

        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(LOGIN_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(RESPONSE_TOKEN));
    }

    @Test
    void login_wrongPassword() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON_VALUE).content(WRONG_PASSWORD_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Declined: User and password don't match or user doesn't exist!"));
    }

    @Test
    void login_unknownUserId() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON_VALUE).content(WRONG_USERID_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Declined: User and password don't match or user doesn't exist!"));
    }

    @Test
    void login_emptyRequestBody() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullUserObject() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON_VALUE).content(FULL_USER_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Wrong request arguments."));
    }

    @Test
    void login_wrongJsonSyntax() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON_VALUE).content(LOGIN_JSON.replaceFirst("(\")", "*")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_emptyJson() throws Exception {
        mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON_VALUE).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("fields 'userId' and 'password' must be defined"));
    }

    @Test
    void getUserIdForToken_success() throws Exception {
        login_successful(); // setup

        mvc.perform(get("/"+RESPONSE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(testUser.getUserId()));
    }

    @Test
    void getUserIdForToken_unknownToken() throws Exception {
        login_successful(); // setup

        mvc.perform(get("/"+RESPONSE_TOKEN+'a'))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserIdForToken_noParameter() throws Exception {
        login_successful(); // setup

        mvc.perform(get("/"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void doesUserExist_Yes() throws Exception {
        login_successful(); // setup

        mvc.perform(get("/user/mmuster"))
                .andExpect(status().isOk());
    }

    @Test
    void doesUserExist_No() throws Exception {
        login_successful(); // setup

        mvc.perform(get("/user/mmuster2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesUserExist_Empty() throws Exception {
        login_successful(); // setup

        mvc.perform(get("/user/"))
                .andExpect(status().isNotFound());
    }
}