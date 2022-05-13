package s0566430.songsMS;

import com.netflix.discovery.EurekaClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private static final String AUTH_SERVICE_NAME = "AUTH";

    private RestTemplate restTemplate;
    private EurekaClient discoveryClient;

    public AuthService(RestTemplate restTemplate, EurekaClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
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
}
