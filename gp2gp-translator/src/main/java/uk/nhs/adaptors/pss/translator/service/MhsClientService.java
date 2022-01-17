package uk.nhs.adaptors.pss.translator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MhsClientService {

    public String send(WebClient.RequestHeadersSpec<? extends WebClient.RequestHeadersSpec<?>> request){
        LOGGER.info("Sending MHS Outbound Request");
        return request.retrieve().bodyToMono(String.class).block();
    }

}
