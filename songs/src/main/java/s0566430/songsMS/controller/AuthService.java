package s0566430.songsMS.controller;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import s0566430.songsMS.model.SongListsXmlRoot;
import s0566430.songsMS.model.SongsXmlRoot;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class AuthService {

    private static final String AUTH_SERVICE_NAME = "AUTH";

    private RestTemplate restTemplate;
    private EurekaClient discoveryClient;

    public AuthService(RestTemplate restTemplate, EurekaClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    static String convertSongToXml(SongsXmlRoot songs) throws JAXBException {
        return convertToXml(songs, SongsXmlRoot.class);
    }

    static String convertSongListToXml(SongListsXmlRoot songLists) throws JAXBException {
        return convertToXml(songLists, SongListsXmlRoot.class);
    }

    private static String convertToXml(Object jaxbElement, Class<?> class_) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(class_);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter sw = new StringWriter();
        marshaller.marshal(jaxbElement, sw);
        return sw.toString();
    }

    static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    String getUserIdForToken(String token) {
        String url = discoveryClient.getNextServerFromEureka(AUTH_SERVICE_NAME, false).getHomePageUrl()
                + token;
        try {
            return restTemplate.getForEntity(url, String.class).getBody();
        }
        catch(HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    boolean doesTokenExist(String token) {
        return getUserIdForToken(token) != null;
    }

    boolean doesTokenMatchUserId(String token, String userId) {
        return getUserIdForToken(token).equals(userId);
    }

    boolean doesUserIdExist(String userId) {
        String url = discoveryClient.getNextServerFromEureka(AUTH_SERVICE_NAME, false).getHomePageUrl()
                + "user/"
                + userId;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK)) return true;
            else return false;
        }
        catch(HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
