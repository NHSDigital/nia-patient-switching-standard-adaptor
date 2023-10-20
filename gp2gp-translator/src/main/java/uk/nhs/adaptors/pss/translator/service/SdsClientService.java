package uk.nhs.adaptors.pss.translator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SdsClientService {

    public String send(WebClient.RequestHeadersSpec<? extends WebClient.RequestHeadersSpec<?>> request) {
        return request.retrieve().bodyToMono(String.class).block();
    }
}
