package uk.nhs.adaptors.pss.translator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;

@Service
public class RequestBuilderService {

    private static final int BYTE_COUNT = 150 * 1024 * 1024;

    @SneakyThrows
    public SslContext buildSSLContext() {
        return SslContextBuilder.forClient().build();
    }

    public ExchangeStrategies buildExchangeStrategies() {
        return ExchangeStrategies
            .builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(BYTE_COUNT))
            .build();
    }
}
