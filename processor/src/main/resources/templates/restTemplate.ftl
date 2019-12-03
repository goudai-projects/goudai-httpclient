package ${packageName};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class Client {

    private final RestTemplate restTemplate;

    @Autowired
    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    <T> T request(URI uri,
                HttpMethod httpMethod,
                HttpHeaders httpHeaders,
                Object body,
                ParameterizedTypeReference<T> responseType) {
            Assert.notNull(uri, "uri must not be null");
            Assert.notNull(responseType, "responseType must not be null");
            HttpEntity httpEntity;
            if (body != null || httpHeaders != null) {
                httpEntity = new HttpEntity(body, httpHeaders);
            } else {
                httpEntity = HttpEntity.EMPTY;
            }
            ;
            return restTemplate.exchange(
                        uri,
                        httpMethod,
                        httpEntity,
                        responseType).getBody();
    }

}
