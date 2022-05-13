package s0566430.songsMS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s0566430.songsMS.model.User;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Random;

@RestController
//@RequestMapping(value = "/auth")
public class UserController {

    private UserDao dao;
    private ObjectMapper mapper = new ObjectMapper();

    /** maps tokens to userId */
    HashMap<String, User> tokenMap = new HashMap<>();

    public UserController(@Qualifier("userDaoImpl") UserDao dao){
        this.dao = dao;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody String userJson) throws IOException {
        try {
            // check for illegal arguments
            if (userJson.toLowerCase().contains("\"firstname\":") || userJson.toLowerCase().contains("\"lastname\":"))
                return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Wrong request arguments.");
            // create non persisted user
            User user = mapper.readValue(userJson, User.class);
            // check for needed arguments
            if (user.getUserId() == null || user.getPassword() == null)
                return ResponseEntity.badRequest().body("fields 'userId' and 'password' must be defined");
            // find fitting user by id
            User existingUser = dao.findUser(user.getUserId());
            if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
                // generate token, put it in hashmap and request that for return body
                String token;
                while (true) { // generate new token if not unique
                    token = generateResponseToken();
                    if (tokenMap.containsKey(token))
                        continue;
                    break;
                }
                tokenMap.put(token, existingUser);
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.TEXT_PLAIN).body("Declined: User and password don't match or user doesn't exist!");
            }
        } catch (PersistenceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(getStackTrace(e));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(getStackTrace(e));
        }
    }

    @GetMapping(value = "/{token}")
    public ResponseEntity<String> getUserIdForToken(@PathVariable String token) {
        User result = tokenMap.get(token);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        else {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(result.getUserId());
        }
    }

    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<String> doesUserExist(@PathVariable String userId) {
         if (dao.findUser(userId) == null)
             return ResponseEntity.notFound().build();
         else
             return ResponseEntity.ok().build();
    }

    String generateResponseToken() {
        char[] array = new char[20];
        for (int i=0; i<array.length; i++) {
            // fill array with random ascii letters and digits
            do array[i] = (char) new Random().nextInt(127);
            while (!Character.isLetterOrDigit(array[i]));
        }
        return new String(array);
    }

    static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
