package ${packageName};

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class Client {

    private final WebClient webClient;

    @Autowired
    public Client(WebClient.Builder builder) {
        this.webClient = builder.build();
    }


    <T> Mono<T> request(URI uri,
        HttpMethod httpMethod,
        HttpHeaders httpHeaders,
        Object body,
        ParameterizedTypeReference<T> responseType) {
        Assert.notNull(uri, "uri must not be null");
        Assert.notNull(responseType, "responseType must not be null");

        WebClient.RequestBodySpec spec = webClient.method(httpMethod)
                .uri(uri)
                .headers(headers -> headers.addAll(httpHeaders));
        if (body != null) {
            spec.body(BodyInserters.fromObject(body));
        }
        return spec.retrieve()
            .bodyToMono(responseType);
    }
}
